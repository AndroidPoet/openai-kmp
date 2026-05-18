package io.github.androidpoet.openai.files

import io.github.androidpoet.openai.client.MultipartPart
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
public data class OpenAIFileObject(
    public val id: String? = null,
    public val bytes: Long? = null,
    @SerialName("created_at")
    public val createdAt: Long? = null,
    public val filename: String? = null,
    public val purpose: String? = null,
    public val `object`: String? = null,
    public val status: String? = null,
)

@Serializable
public data class OpenAIFileDeleted(
    public val id: String? = null,
    public val deleted: Boolean = false,
    public val `object`: String? = null,
)

@Serializable
public data class FileDeleteBody(
    @SerialName("force")
    public val force: Boolean? = null,
)

public interface FilesClient {
    public suspend fun create(
        purpose: String,
        bytes: ByteArray,
        filename: String,
        contentType: String = "application/octet-stream",
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun retrieve(fileId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>

    public suspend fun list(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>

    public suspend fun delete(
        fileId: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun content(fileId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class FilesClientImpl(private val client: OpenAIClient) : FilesClient {
    override suspend fun create(
        purpose: String,
        bytes: ByteArray,
        filename: String,
        contentType: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.postMultipart(
        endpoint = "files",
        parts = listOf(
            MultipartPart.Text(name = "purpose", value = purpose),
            MultipartPart.Binary(name = "file", bytes = bytes, filename = filename, contentType = contentType),
        ),
        headers = headers,
    )

    override suspend fun retrieve(fileId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpointPath("files", fileId), headers = headers)

    override suspend fun list(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("files", queryParams, headers)

    override suspend fun delete(
        fileId: String,
        body: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.delete(endpointPath("files", fileId), body, headers)

    override suspend fun content(fileId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get(endpointPath("files", fileId, "content"), headers = headers)
}

public fun OpenAIClient.files(): FilesClient = FilesClientImpl(this)

public suspend fun FilesClient.retrieveTyped(
    fileId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIFileObject> = retrieve(fileId, headers).deserialize()

public suspend fun FilesClient.listTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<OpenAIFileObject>> = list(queryParams, headers).deserialize()

public suspend fun FilesClient.deleteTyped(
    fileId: String,
    body: FileDeleteBody? = null,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIFileDeleted> = delete(fileId, body?.toJsonBody(defaultJson), headers).deserialize()

public suspend fun FilesClient.contentJsonTyped(
    fileId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<JsonElement> = content(fileId, headers).deserialize()
