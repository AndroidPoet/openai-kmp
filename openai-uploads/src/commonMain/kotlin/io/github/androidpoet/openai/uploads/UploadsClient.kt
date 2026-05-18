package io.github.androidpoet.openai.uploads

import io.github.androidpoet.openai.client.MultipartPart
import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.endpointPath
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class UploadCreateRequest(
    public val purpose: String,
    public val filename: String,
    @SerialName("bytes")
    public val bytesCount: Long,
    @SerialName("mime_type")
    public val mimeType: String,
)

@Serializable
public data class UploadCompleteRequest(
    @SerialName("part_ids")
    public val partIds: List<String>,
)

@Serializable
public data class UploadObject(
    public val id: String? = null,
    public val status: String? = null,
    public val filename: String? = null,
    public val purpose: String? = null,
    public val `object`: String? = null,
    public val bytes: Long? = null,
    public val expires_at: Long? = null,
    public val file: JsonElement? = null,
)

public interface UploadsClient {
    public suspend fun create(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun cancel(uploadId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun complete(uploadId: String, body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun addPart(
        uploadId: String,
        bytes: ByteArray,
        filename: String,
        contentType: String = "application/octet-stream",
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>
}

internal class UploadsClientImpl(private val client: OpenAIClient) : UploadsClient {
    override suspend fun create(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("uploads", body, headers)

    override suspend fun cancel(uploadId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post(endpointPath("uploads", uploadId, "cancel"), headers = headers)

    override suspend fun complete(
        uploadId: String,
        body: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.post(endpointPath("uploads", uploadId, "complete"), body, headers)

    override suspend fun addPart(
        uploadId: String,
        bytes: ByteArray,
        filename: String,
        contentType: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.postMultipart(
        endpoint = endpointPath("uploads", uploadId, "parts"),
        parts = listOf(
            MultipartPart.Binary("data", bytes, filename, contentType),
        ),
        headers = headers,
    )
}

public fun OpenAIClient.uploads(): UploadsClient = UploadsClientImpl(this)

public suspend fun UploadsClient.createTyped(
    request: UploadCreateRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<UploadObject> = create(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun UploadsClient.completeTyped(
    uploadId: String,
    request: UploadCompleteRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<UploadObject> = complete(uploadId, request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun UploadsClient.cancelTyped(
    uploadId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<UploadObject> = cancel(uploadId, headers).deserialize()
