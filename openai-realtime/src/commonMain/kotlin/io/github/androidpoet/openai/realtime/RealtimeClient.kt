package io.github.androidpoet.openai.realtime

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.core.result.OpenAIError
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
public data class RealtimeEnvelope(
    public val type: String,
    @SerialName("event_id") public val eventId: String? = null,
    public val session: JsonObject? = null,
    public val response: JsonObject? = null,
    public val item: JsonObject? = null,
    public val delta: JsonElement? = null,
    public val error: JsonObject? = null,
)

@Serializable
public enum class QueueOverflowPolicy {
    Fail,
    DropLatest,
    DropOldest,
}

@Serializable
public data class RealtimeSocketConfig(
    public val maxReconnectAttempts: Int = Int.MAX_VALUE,
    public val initialReconnectDelayMillis: Long = 500,
    public val maxReconnectDelayMillis: Long = 8_000,
    public val heartbeatIntervalMillis: Long = 15_000,
    /**
     * Outgoing queue capacity for messages while reconnecting.
     * Use [Channel.UNLIMITED] for unbounded buffering.
     */
    public val outgoingQueueCapacity: Int = Channel.UNLIMITED,
    public val queueOverflowPolicy: QueueOverflowPolicy = QueueOverflowPolicy.Fail,
)

public interface RealtimeSocket {
    public suspend fun connect(url: String, headers: Map<String, String> = emptyMap())
    public suspend fun send(text: String)
    public fun incoming(): Flow<String>
    public suspend fun close()
}

public interface RealtimeClient {
    public suspend fun connect(
        model: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: List<Pair<String, String>> = emptyList(),
    ): OpenAIResult<Unit>

    public suspend fun send(event: RealtimeEnvelope): OpenAIResult<Unit>
    public suspend fun sendRaw(json: String): OpenAIResult<Unit>
    public fun incomingRaw(): Flow<OpenAIResult<String>>
    public fun incomingTyped(): Flow<OpenAIResult<RealtimeEnvelope>>
    public suspend fun close(): OpenAIResult<Unit>
}

internal class RealtimeClientImpl(
    private val baseUrl: String,
    private val apiKey: String,
    private val socket: RealtimeSocket,
) : RealtimeClient {
    override suspend fun connect(
        model: String,
        headers: Map<String, String>,
        queryParams: List<Pair<String, String>>,
    ): OpenAIResult<Unit> =
        runCatchingResult {
            val params = buildList {
                add("model" to model)
                addAll(queryParams)
            }
            socket.connect(
                url = realtimeUrl(baseUrl, params),
                headers = mapOf("Authorization" to "Bearer $apiKey") + headers,
            )
        }

    override suspend fun send(event: RealtimeEnvelope): OpenAIResult<Unit> =
        sendRaw(defaultJson.encodeToString(RealtimeEnvelope.serializer(), event))

    override suspend fun sendRaw(json: String): OpenAIResult<Unit> =
        runCatchingResult {
            socket.send(json)
        }

    override fun incomingRaw(): Flow<OpenAIResult<String>> =
        socket
            .incoming()
            .map<String, OpenAIResult<String>> { OpenAIResult.Success(it) }
            .catch { emit(OpenAIResult.Failure(OpenAIError(message = it.message ?: "Realtime stream error"))) }

    override fun incomingTyped(): Flow<OpenAIResult<RealtimeEnvelope>> =
        incomingRaw().map { result ->
            when (result) {
                is OpenAIResult.Success -> {
                    try {
                        val event = defaultJson.decodeFromString(RealtimeEnvelope.serializer(), result.value)
                        OpenAIResult.Success(event)
                    } catch (e: Exception) {
                        OpenAIResult.Failure(OpenAIError(message = e.message ?: "Invalid realtime payload"))
                    }
                }
                is OpenAIResult.Failure -> result
            }
        }

    override suspend fun close(): OpenAIResult<Unit> =
        runCatchingResult {
            socket.close()
        }
}

public class KtorRealtimeSocket(
    engineFactory: HttpClientEngineFactory<*> = realtimePlatformEngine(),
    private val config: RealtimeSocketConfig = RealtimeSocketConfig(),
) : RealtimeSocket {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val incomingMessages = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 128)
    private val outgoingQueue = Channel<String>(config.outgoingQueueCapacity)
    private val client = HttpClient(engineFactory) { install(WebSockets) }

    private var connectionJob: Job? = null
    private var shouldRun: Boolean = false

    override suspend fun connect(url: String, headers: Map<String, String>) {
        shouldRun = true
        if (connectionJob?.isActive == true) return

        connectionJob = scope.launch {
            var attempts = 0
            var reconnectDelay = config.initialReconnectDelayMillis.coerceAtLeast(100)

            while (isActive && shouldRun) {
                try {
                    val session = client.webSocketSession {
                        url(url)
                        headers.forEach { (key, value) -> header(key, value) }
                    }
                    if (!session.isActive) throw IllegalStateException("Realtime websocket failed to connect")

                    attempts = 0
                    reconnectDelay = config.initialReconnectDelayMillis.coerceAtLeast(100)

                    val senderJob = launch {
                        while (isActive && session.isActive && shouldRun) {
                            val payload = outgoingQueue.receive()
                            session.send(Frame.Text(payload))
                        }
                    }

                    val heartbeatJob = launch {
                        while (isActive && session.isActive && shouldRun) {
                            delay(config.heartbeatIntervalMillis)
                            if (session.isActive && shouldRun) {
                                session.send(Frame.Ping(byteArrayOf()))
                            }
                        }
                    }

                    try {
                        for (frame in session.incoming) {
                            when (frame) {
                                is Frame.Text -> incomingMessages.emit(frame.readText())
                                is Frame.Close -> break
                                else -> Unit
                            }
                        }
                    } finally {
                        heartbeatJob.cancel()
                        senderJob.cancel()
                    }
                } catch (e: Exception) {
                    emitSocketError(
                        message = e.message ?: "Realtime connection error",
                        retrying = attempts < config.maxReconnectAttempts,
                        terminal = attempts >= config.maxReconnectAttempts,
                    )
                }

                if (!shouldRun || !isActive) break
                attempts += 1
                if (attempts > config.maxReconnectAttempts) {
                    emitSocketError(
                        message = "Realtime reconnect limit reached",
                        retrying = false,
                        terminal = true,
                    )
                    break
                }
                delay(reconnectDelay)
                reconnectDelay = (reconnectDelay * 2).coerceAtMost(config.maxReconnectDelayMillis.coerceAtLeast(reconnectDelay))
            }
        }
    }

    override suspend fun send(text: String) {
        if (!shouldRun) throw IllegalStateException("Realtime socket is not started")
        when (config.queueOverflowPolicy) {
            QueueOverflowPolicy.Fail -> {
                val enqueued = outgoingQueue.trySend(text)
                if (!enqueued.isSuccess) throw IllegalStateException("Failed to enqueue realtime message")
            }
            QueueOverflowPolicy.DropLatest -> {
                outgoingQueue.trySend(text)
            }
            QueueOverflowPolicy.DropOldest -> {
                val enqueued = outgoingQueue.trySend(text)
                if (!enqueued.isSuccess) {
                    outgoingQueue.tryReceive()
                    val secondTry = outgoingQueue.trySend(text)
                    if (!secondTry.isSuccess) throw IllegalStateException("Failed to enqueue realtime message")
                }
            }
        }
    }

    override fun incoming(): Flow<String> = incomingMessages

    override suspend fun close() {
        shouldRun = false
        connectionJob?.cancel()
        connectionJob = null
        outgoingQueue.close()
        client.close()
        scope.cancel()
    }

    private suspend fun emitSocketError(
        message: String,
        retrying: Boolean,
        terminal: Boolean,
    ) {
        val event = buildJsonObject {
            put("type", "sdk.error")
            put("message", message)
            put("retrying", retrying)
            put("terminal", terminal)
        }
        incomingMessages.emit(defaultJson.encodeToString(JsonObject.serializer(), event))
    }
}

public fun OpenAIClient.realtime(socket: RealtimeSocket): RealtimeClient =
    RealtimeClientImpl(baseUrl = baseUrl, apiKey = apiKey, socket = socket)

public fun OpenAIClient.realtime(config: RealtimeSocketConfig = RealtimeSocketConfig()): RealtimeClient =
    realtime(KtorRealtimeSocket(config = config))

private fun realtimeUrl(baseUrl: String, queryParams: List<Pair<String, String>>): String {
    val root = baseUrl.removeSuffix("/").removeSuffix("/v1")
    val wsRoot = when {
        root.startsWith("https://") -> "wss://${root.removePrefix("https://")}"
        root.startsWith("http://") -> "ws://${root.removePrefix("http://")}"
        else -> root
    }
    val query = queryParams.joinToString("&") { (key, value) -> "${encodeQueryComponent(key)}=${encodeQueryComponent(value)}" }
    return if (query.isEmpty()) "$wsRoot/v1/realtime" else "$wsRoot/v1/realtime?$query"
}

private fun encodeQueryComponent(value: String): String {
    val bytes = value.encodeToByteArray()
    val out = StringBuilder(bytes.size)
    bytes.forEach { b ->
        val c = b.toInt().toChar()
        if (c.isAsciiUnreserved()) {
            out.append(c)
        } else {
            val v = b.toInt() and 0xff
            out.append('%')
            out.append("0123456789ABCDEF"[v ushr 4])
            out.append("0123456789ABCDEF"[v and 0x0f])
        }
    }
    return out.toString()
}

private fun Char.isAsciiUnreserved(): Boolean =
    (this in 'a'..'z') || (this in 'A'..'Z') || (this in '0'..'9') || this == '-' || this == '_' || this == '.' || this == '~'

private suspend inline fun runCatchingResult(crossinline block: suspend () -> Unit): OpenAIResult<Unit> =
    try {
        block()
        OpenAIResult.Success(Unit)
    } catch (e: Exception) {
        OpenAIResult.Failure(OpenAIError(message = e.message ?: "Realtime operation failed"))
    }
