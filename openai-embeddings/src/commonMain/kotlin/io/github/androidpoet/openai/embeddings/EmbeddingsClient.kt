package io.github.androidpoet.openai.embeddings

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class EmbeddingRequest(
    public val model: String,
    public val input: JsonElement,
    public val dimensions: Int? = null,
    @SerialName("encoding_format")
    public val encodingFormat: String? = null,
)

@Serializable
public data class EmbeddingResponse(
    public val data: List<EmbeddingData> = emptyList(),
    public val model: String? = null,
    public val usage: JsonElement? = null,
)

@Serializable
public data class EmbeddingData(
    public val index: Int? = null,
    public val embedding: List<Double> = emptyList(),
    public val `object`: String? = null,
)

public interface EmbeddingsClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class EmbeddingsClientImpl(private val client: OpenAIClient) : EmbeddingsClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("embeddings", body, headers)
}

public fun OpenAIClient.embeddings(): EmbeddingsClient = EmbeddingsClientImpl(this)

public suspend fun EmbeddingsClient.createTyped(
    request: EmbeddingRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<EmbeddingResponse> = create(request.toJsonBody(defaultJson), headers).deserialize()
