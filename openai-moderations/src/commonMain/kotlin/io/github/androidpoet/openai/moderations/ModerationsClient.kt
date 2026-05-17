package io.github.androidpoet.openai.moderations

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ModerationRequest(
    public val model: String? = null,
    public val input: JsonElement,
)

@Serializable
public data class ModerationResponse(
    public val id: String? = null,
    public val model: String? = null,
    public val results: JsonElement? = null,
)

public interface ModerationsClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class ModerationsClientImpl(private val client: OpenAIClient) : ModerationsClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("moderations", body, headers)
}

public fun OpenAIClient.moderations(): ModerationsClient = ModerationsClientImpl(this)

public suspend fun ModerationsClient.createTyped(
    request: ModerationRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ModerationResponse> = create(request.toJsonBody(defaultJson), headers).deserialize()
