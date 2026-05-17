package io.github.androidpoet.openai.images

import io.github.androidpoet.openai.client.MultipartPart
import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ImageGenerationRequest(
    public val model: String? = null,
    public val prompt: String,
    public val n: Int? = null,
    public val size: String? = null,
    public val quality: String? = null,
    @SerialName("response_format")
    public val responseFormat: String? = null,
    public val user: String? = null,
)

@Serializable
public data class ImageGenerationResponse(
    public val created: Long? = null,
    public val data: JsonElement? = null,
)

public interface ImagesClient {
    public suspend fun generate(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>

    public suspend fun edit(
        imageBytes: ByteArray,
        imageFilename: String,
        prompt: String,
        imageContentType: String = "image/png",
        maskBytes: ByteArray? = null,
        maskFilename: String = "mask.png",
        maskContentType: String = "image/png",
        model: String? = null,
        size: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun variation(
        imageBytes: ByteArray,
        imageFilename: String,
        imageContentType: String = "image/png",
        model: String? = null,
        n: Int? = null,
        size: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>
}

internal class ImagesClientImpl(private val client: OpenAIClient) : ImagesClient {
    override suspend fun generate(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("images/generations", body, headers)

    override suspend fun edit(
        imageBytes: ByteArray,
        imageFilename: String,
        prompt: String,
        imageContentType: String,
        maskBytes: ByteArray?,
        maskFilename: String,
        maskContentType: String,
        model: String?,
        size: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> {
        val parts = buildList {
            add(MultipartPart.Binary("image", imageBytes, imageFilename, imageContentType))
            add(MultipartPart.Text("prompt", prompt))
            model?.let { add(MultipartPart.Text("model", it)) }
            size?.let { add(MultipartPart.Text("size", it)) }
            if (maskBytes != null) {
                add(MultipartPart.Binary("mask", maskBytes, maskFilename, maskContentType))
            }
        }
        return client.postMultipart("images/edits", parts, headers)
    }

    override suspend fun variation(
        imageBytes: ByteArray,
        imageFilename: String,
        imageContentType: String,
        model: String?,
        n: Int?,
        size: String?,
        headers: Map<String, String>,
    ): OpenAIResult<String> {
        val parts = buildList {
            add(MultipartPart.Binary("image", imageBytes, imageFilename, imageContentType))
            model?.let { add(MultipartPart.Text("model", it)) }
            n?.let { add(MultipartPart.Text("n", it.toString())) }
            size?.let { add(MultipartPart.Text("size", it)) }
        }
        return client.postMultipart("images/variations", parts, headers)
    }
}

public fun OpenAIClient.images(): ImagesClient = ImagesClientImpl(this)

public suspend fun ImagesClient.generateTyped(
    request: ImageGenerationRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<ImageGenerationResponse> = generate(request.toJsonBody(defaultJson), headers).deserialize()
