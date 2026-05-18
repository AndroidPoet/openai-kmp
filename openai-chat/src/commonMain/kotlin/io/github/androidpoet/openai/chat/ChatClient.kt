package io.github.androidpoet.openai.chat

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.endpointPath
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.models.OpenAIListResponse
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ChatCompletionRequest(
    public val model: String,
    public val messages: JsonElement,
    public val temperature: Double? = null,
    public val top_p: Double? = null,
    public val stream: Boolean? = null,
    public val tools: JsonElement? = null,
)

@Serializable
public data class ChatCompletionObject(
    public val id: String? = null,
    public val model: String? = null,
    public val choices: JsonElement? = null,
    public val usage: JsonElement? = null,
)

public interface ChatClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun retrieve(completionId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun update(
        completionId: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>
    public suspend fun list(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun delete(completionId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun messages(completionId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class ChatClientImpl(private val client: OpenAIClient) : ChatClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("chat/completions", body, headers)

    override suspend fun retrieve(completionId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpointPath("chat", "completions", completionId), headers = headers)

    override suspend fun update(
        completionId: String,
        body: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.post(endpointPath("chat", "completions", completionId), body, headers)

    override suspend fun list(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("chat/completions", queryParams, headers)

    override suspend fun delete(completionId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.delete(endpointPath("chat", "completions", completionId), headers = headers)

    override suspend fun messages(completionId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpointPath("chat", "completions", completionId, "messages"), headers = headers)
}

public fun OpenAIClient.chat(): ChatClient = ChatClientImpl(this)

public suspend fun ChatClient.createTyped(
    request: ChatCompletionRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ChatCompletionObject> = create(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun ChatClient.retrieveTyped(
    completionId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ChatCompletionObject> = retrieve(completionId, headers).deserialize()

public suspend fun ChatClient.listTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<ChatCompletionObject>> = list(queryParams, headers).deserialize()

public suspend fun ChatClient.messagesTyped(
    completionId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<JsonElement>> = messages(completionId, headers).deserialize()
