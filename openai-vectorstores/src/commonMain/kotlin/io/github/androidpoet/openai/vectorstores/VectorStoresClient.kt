package io.github.androidpoet.openai.vectorstores

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
public data class VectorStoreCreateRequest(
    public val name: String? = null,
    @SerialName("file_ids")
    public val fileIds: List<String>? = null,
    public val metadata: Map<String, String>? = null,
    @SerialName("expires_after")
    public val expiresAfter: JsonElement? = null,
)

@Serializable
public data class VectorStoreObject(
    public val id: String? = null,
    public val name: String? = null,
    public val status: String? = null,
    @SerialName("file_counts")
    public val fileCounts: JsonElement? = null,
    public val metadata: Map<String, String>? = null,
)

@Serializable
public data class VectorStoreSearchRequest(
    public val query: String,
    @SerialName("max_num_results")
    public val maxNumResults: Int? = null,
    @SerialName("filters")
    public val filters: JsonElement? = null,
)

public interface VectorStoresClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun retrieve(vectorStoreId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun update(vectorStoreId: String, body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun list(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun delete(vectorStoreId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun search(vectorStoreId: String, body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun addFile(vectorStoreId: String, body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun retrieveFile(vectorStoreId: String, fileId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun listFiles(vectorStoreId: String, queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun deleteFile(vectorStoreId: String, fileId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class VectorStoresClientImpl(private val client: OpenAIClient) : VectorStoresClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("vector_stores", body, headers)

    override suspend fun retrieve(vectorStoreId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get("vector_stores/$vectorStoreId", headers = headers)

    override suspend fun update(
        vectorStoreId: String,
        body: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.post("vector_stores/$vectorStoreId", body, headers)

    override suspend fun list(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("vector_stores", queryParams, headers)

    override suspend fun delete(vectorStoreId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.delete("vector_stores/$vectorStoreId", headers = headers)

    override suspend fun search(
        vectorStoreId: String,
        body: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.post("vector_stores/$vectorStoreId/search", body, headers)

    override suspend fun addFile(
        vectorStoreId: String,
        body: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.post("vector_stores/$vectorStoreId/files", body, headers)

    override suspend fun retrieveFile(
        vectorStoreId: String,
        fileId: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("vector_stores/$vectorStoreId/files/$fileId", headers = headers)

    override suspend fun listFiles(
        vectorStoreId: String,
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("vector_stores/$vectorStoreId/files", queryParams, headers)

    override suspend fun deleteFile(
        vectorStoreId: String,
        fileId: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.delete("vector_stores/$vectorStoreId/files/$fileId", headers = headers)
}

public fun OpenAIClient.vectorStores(): VectorStoresClient = VectorStoresClientImpl(this)

public suspend fun VectorStoresClient.createTyped(
    request: VectorStoreCreateRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<VectorStoreObject> = create(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun VectorStoresClient.retrieveTyped(
    vectorStoreId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<VectorStoreObject> = retrieve(vectorStoreId, headers).deserialize()

public suspend fun VectorStoresClient.listTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<VectorStoreObject>> = list(queryParams, headers).deserialize()

public suspend fun VectorStoresClient.searchTyped(
    vectorStoreId: String,
    request: VectorStoreSearchRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<JsonElement> = search(vectorStoreId, request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun VectorStoresClient.listFilesTyped(
    vectorStoreId: String,
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<JsonElement>> = listFiles(vectorStoreId, queryParams, headers).deserialize()
