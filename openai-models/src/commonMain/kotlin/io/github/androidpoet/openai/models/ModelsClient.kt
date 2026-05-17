package io.github.androidpoet.openai.models

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.core.models.OpenAIListResponse
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ModelObject(
    public val id: String? = null,
    public val `object`: String? = null,
    @SerialName("created")
    public val createdAt: Long? = null,
    @SerialName("owned_by")
    public val ownedBy: String? = null,
)

@Serializable
public data class ModelDeleted(
    public val id: String? = null,
    public val deleted: Boolean = false,
    public val `object`: String? = null,
)

public interface ModelsClient {
    public suspend fun retrieve(model: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun list(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun delete(model: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class ModelsClientImpl(private val client: OpenAIClient) : ModelsClient {
    override suspend fun retrieve(model: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get("models/$model", headers = headers)

    override suspend fun list(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("models", queryParams, headers)

    override suspend fun delete(model: String, headers: Map<String, String>): OpenAIResult<String> =
        client.delete("models/$model", headers = headers)
}

public fun OpenAIClient.models(): ModelsClient = ModelsClientImpl(this)

public suspend fun ModelsClient.retrieveTyped(
    model: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ModelObject> = retrieve(model, headers).deserialize()

public suspend fun ModelsClient.listTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<ModelObject>> = list(queryParams, headers).deserialize()

public suspend fun ModelsClient.deleteTyped(
    model: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ModelDeleted> = delete(model, headers).deserialize()
