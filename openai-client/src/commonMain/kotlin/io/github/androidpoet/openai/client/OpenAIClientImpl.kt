package io.github.androidpoet.openai.client

import io.github.androidpoet.openai.client.transport.HttpTransport
import io.github.androidpoet.openai.core.result.OpenAIResult

internal class OpenAIClientImpl(
    override val baseUrl: String,
    override val apiKey: String,
    private val transport: HttpTransport,
) : OpenAIClient {
    override suspend fun get(
        endpoint: String,
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.get(url = resolve(endpoint), queryParams = queryParams, headers = headers)

    override suspend fun post(
        endpoint: String,
        body: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.post(url = resolve(endpoint), body = body, headers = headers)

    override suspend fun patch(
        endpoint: String,
        body: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.patch(url = resolve(endpoint), body = body, headers = headers)

    override suspend fun put(
        endpoint: String,
        body: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.put(url = resolve(endpoint), body = body, headers = headers)

    override suspend fun delete(
        endpoint: String,
        body: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.delete(url = resolve(endpoint), body = body, headers = headers)

    override suspend fun postMultipart(
        endpoint: String,
        parts: List<MultipartPart>,
        headers: Map<String, String>,
    ): OpenAIResult<String> =
        transport.postMultipart(url = resolve(endpoint), parts = parts, headers = headers)

    override fun close() {
        transport.close()
    }

    private fun resolve(endpoint: String): String {
        val normalizedBase = baseUrl.trimEnd('/')
        val normalizedEndpoint = endpoint.trimStart('/')
        return "$normalizedBase/$normalizedEndpoint"
    }
}
