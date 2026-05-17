package io.github.androidpoet.openai.client.transport

import io.github.androidpoet.openai.client.MultipartPart
import io.github.androidpoet.openai.client.OpenAIConfig
import io.github.androidpoet.openai.core.result.OpenAIError
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val CLIENT_VERSION = "openai-kmp/0.1.0"

internal class HttpTransport(
    private val config: OpenAIConfig,
    engineFactory: HttpClientEngineFactory<*>,
    private val baseUrl: String,
    private val apiKey: String,
) {
    private val errorJson = Json { ignoreUnknownKeys = true }

    internal val httpClient: HttpClient = HttpClient(engineFactory) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMillis
            connectTimeoutMillis = config.timeoutMillis
            socketTimeoutMillis = config.timeoutMillis
        }
        if (config.logging) {
            install(Logging) {
                level = config.logLevel
            }
        }
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header("X-Client-Info", CLIENT_VERSION)
            config.organization?.let { header("OpenAI-Organization", it) }
            config.project?.let { header("OpenAI-Project", it) }
            config.headers.forEach { (key, value) -> header(key, value) }
        }
    }

    suspend fun get(
        url: String,
        queryParams: List<Pair<String, String>> = emptyList(),
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.get(normalize(url)) {
            queryParams.forEach { (k, v) -> this.url.parameters.append(k, v) }
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    suspend fun post(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.post(normalize(url)) {
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    suspend fun patch(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.patch(normalize(url)) {
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    suspend fun put(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.put(normalize(url)) {
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    suspend fun delete(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.delete(normalize(url)) {
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    suspend fun postMultipart(
        url: String,
        parts: List<MultipartPart>,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String> = execute {
        httpClient.post(normalize(url)) {
            val payload = MultiPartFormDataContent(
                formData {
                    parts.forEach { part ->
                        when (part) {
                            is MultipartPart.Text -> append(part.name, part.value)
                            is MultipartPart.Binary -> append(
                                key = part.name,
                                value = part.bytes,
                                headers = io.ktor.http.Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"${part.filename}\"")
                                    append(HttpHeaders.ContentType, part.contentType)
                                },
                            )
                        }
                    }
                },
            )
            setBody(payload)
            headers.forEach { (k, v) -> header(k, v) }
        }
    }

    fun close() {
        httpClient.close()
    }

    private suspend inline fun execute(crossinline request: suspend () -> HttpResponse): OpenAIResult<String> =
        try {
            val response = request()
            val text = response.bodyAsText()
            if (response.status.isSuccess()) {
                OpenAIResult.Success(text)
            } else {
                OpenAIResult.Failure(parseError(text, response.status.value))
            }
        } catch (e: Exception) {
            OpenAIResult.Failure(OpenAIError(message = e.message ?: "Unknown network error"))
        }

    private fun parseError(body: String, statusCode: Int): OpenAIError =
        try {
            val envelope = errorJson.decodeFromString(OpenAIErrorEnvelope.serializer(), body)
            val inner = envelope.error
            if (inner != null) {
                OpenAIError(
                    message = inner.message ?: "HTTP $statusCode",
                    code = inner.code,
                    type = inner.type,
                    param = inner.param,
                    statusCode = statusCode,
                )
            } else {
                OpenAIError(message = body.ifBlank { "HTTP $statusCode" }, statusCode = statusCode)
            }
        } catch (_: Exception) {
            OpenAIError(message = body.ifBlank { "HTTP $statusCode" }, statusCode = statusCode)
        }

    private fun normalize(url: String): String {
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        return "${baseUrl.trimEnd('/')}/${url.trimStart('/')}"
    }
}

@Serializable
private data class OpenAIErrorEnvelope(
    val error: OpenAIErrorPayload? = null,
)

@Serializable
private data class OpenAIErrorPayload(
    val message: String? = null,
    val type: String? = null,
    val param: String? = null,
    @SerialName("code")
    val code: String? = null,
)
