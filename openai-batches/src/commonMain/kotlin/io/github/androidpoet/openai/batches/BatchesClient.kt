package io.github.androidpoet.openai.batches

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
public data class BatchCreateRequest(
    @SerialName("input_file_id")
    public val inputFileId: String,
    public val endpoint: String,
    @SerialName("completion_window")
    public val completionWindow: String,
    public val metadata: Map<String, String>? = null,
)

@Serializable
public data class BatchObject(
    public val id: String? = null,
    public val status: String? = null,
    public val endpoint: String? = null,
    @SerialName("input_file_id")
    public val inputFileId: String? = null,
    @SerialName("output_file_id")
    public val outputFileId: String? = null,
    @SerialName("error_file_id")
    public val errorFileId: String? = null,
    public val errors: JsonElement? = null,
)

public interface BatchesClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun retrieve(batchId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun list(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun cancel(batchId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class BatchesClientImpl(private val client: OpenAIClient) : BatchesClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("batches", body, headers)

    override suspend fun retrieve(batchId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpointPath("batches", batchId), headers = headers)

    override suspend fun list(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("batches", queryParams, headers)

    override suspend fun cancel(batchId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpointPath("batches", batchId, "cancel"), headers = headers)
}

public fun OpenAIClient.batches(): BatchesClient = BatchesClientImpl(this)

public suspend fun BatchesClient.createTyped(
    request: BatchCreateRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<BatchObject> = create(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun BatchesClient.retrieveTyped(
    batchId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<BatchObject> = retrieve(batchId, headers).deserialize()

public suspend fun BatchesClient.listTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<BatchObject>> = list(queryParams, headers).deserialize()
