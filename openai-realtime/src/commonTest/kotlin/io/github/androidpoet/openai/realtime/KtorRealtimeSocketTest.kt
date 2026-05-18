package io.github.androidpoet.openai.realtime

import io.github.androidpoet.openai.client.OpenAI
import io.github.androidpoet.openai.core.result.OpenAIResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorRealtimeSocketTest {
    @Test
    fun test_connect_buildsRealtimeUrl_andAuthorizationHeader() = runTest {
        val socket = FakeRealtimeSocket()
        val client = OpenAI.create(apiKey = "test-key")
        val realtime = client.realtime(socket)

        val result = realtime.connect(model = "gpt-realtime", queryParams = listOf("voice" to "alloy"))

        assertTrue(result is OpenAIResult.Success)
        assertEquals("wss://api.openai.com/v1/realtime?model=gpt-realtime&voice=alloy", socket.connectedUrl)
        assertEquals("Bearer test-key", socket.connectedHeaders["Authorization"])
    }

    @Test
    fun test_incomingTyped_invalidJson_returnsFailure() = runTest {
        val socket = FakeRealtimeSocket()
        val client = OpenAI.create(apiKey = "test-key")
        val realtime = client.realtime(socket)

        socket.emit("not-json")

        val next = realtime.incomingTyped().first()
        assertTrue(next is OpenAIResult.Failure)
    }

    @Test
    fun test_send_whenSocketNotStarted_returnsFailureFromRealtimeClient() = runTest {
        val socket = KtorRealtimeSocket(
            config = RealtimeSocketConfig(outgoingQueueCapacity = 1),
        )
        val client = OpenAI.create(apiKey = "test-key")
        val realtime = client.realtime(socket)

        val result = realtime.sendRaw("""{"type":"x"}""")
        assertTrue(result is OpenAIResult.Failure)

        socket.close()
    }
}

private class FakeRealtimeSocket : RealtimeSocket {
    var connectedUrl: String = ""
    var connectedHeaders: Map<String, String> = emptyMap()

    private val stream = MutableSharedFlow<String>(replay = 1)

    override suspend fun connect(url: String, headers: Map<String, String>) {
        connectedUrl = url
        connectedHeaders = headers
    }

    override suspend fun send(text: String) = Unit

    override fun incoming(): Flow<String> = stream

    override suspend fun close() = Unit

    suspend fun emit(value: String) {
        stream.emit(value)
    }
}
