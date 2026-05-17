package io.github.androidpoet.openai.responses

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.models.OpenAIListResponse
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ResponseCreateRequest(
    public val model: String? = null,
    public val input: JsonElement? = null,
    public val instructions: String? = null,
    public val metadata: Map<String, String>? = null,
    public val stream: Boolean? = null,
)

@Serializable
public data class ResponseCompactRequest(
    @SerialName("response_ids")
    public val responseIds: List<String>,
)

@Serializable
public data class OpenAIResponseObject(
    public val id: String? = null,
    public val status: String? = null,
    @SerialName("output")
    public val output: JsonElement? = null,
    @SerialName("usage")
    public val usage: JsonElement? = null,
)

@Serializable
public data class ResponseInputTokenObject(
    public val id: String? = null,
    @SerialName("input_tokens")
    public val inputTokens: Int? = null,
    @SerialName("cached_tokens")
    public val cachedTokens: Int? = null,
)

public interface ResponsesClient {
    public suspend fun create(
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun retrieve(
        responseId: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun delete(
        responseId: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun cancel(
        responseId: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun compact(
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun inputItems(
        responseId: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun inputTokens(
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>
}

internal class ResponsesClientImpl(private val client: OpenAIClient) : ResponsesClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpoint = "responses", body = body, headers = headers)

    override suspend fun retrieve(responseId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpoint = "responses/$responseId", headers = headers)

    override suspend fun delete(responseId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.delete(endpoint = "responses/$responseId", headers = headers)

    override suspend fun cancel(responseId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpoint = "responses/$responseId/cancel", headers = headers)

    override suspend fun compact(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpoint = "responses/compact", body = body, headers = headers)

    override suspend fun inputItems(responseId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpoint = "responses/$responseId/input_items", headers = headers)

    override suspend fun inputTokens(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpoint = "responses/input_tokens", body = body, headers = headers)
}

public fun OpenAIClient.responses(): ResponsesClient = ResponsesClientImpl(this)

public suspend fun ResponsesClient.createTyped(
    request: ResponseCreateRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIResponseObject> = create(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun ResponsesClient.retrieveTyped(
    responseId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIResponseObject> = retrieve(responseId, headers).deserialize()

public suspend fun ResponsesClient.compactTyped(
    request: ResponseCompactRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIResponseObject> = compact(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun ResponsesClient.inputItemsTyped(
    responseId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<JsonElement>> = inputItems(responseId, headers).deserialize()

public suspend fun ResponsesClient.inputTokensTyped(
    body: JsonElement,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ResponseInputTokenObject> = inputTokens(body.toJsonBody(defaultJson), headers).deserialize()
