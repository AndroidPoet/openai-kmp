package io.github.androidpoet.openai.client

import io.github.androidpoet.openai.core.result.OpenAIResult

public sealed interface MultipartPart {
    public data class Text(val name: String, val value: String) : MultipartPart

    public data class Binary(
        val name: String,
        val bytes: ByteArray,
        val filename: String,
        val contentType: String,
    ) : MultipartPart
}

public interface OpenAIClient {
    public val baseUrl: String
    public val apiKey: String

    public suspend fun get(
        endpoint: String,
        queryParams: List<Pair<String, String>> = emptyList(),
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun post(
        endpoint: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun patch(
        endpoint: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun put(
        endpoint: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun delete(
        endpoint: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun postMultipart(
        endpoint: String,
        parts: List<MultipartPart>,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public fun close()
}
