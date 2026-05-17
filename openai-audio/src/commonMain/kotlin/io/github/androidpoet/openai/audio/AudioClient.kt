package io.github.androidpoet.openai.audio

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
public data class SpeechRequest(
    public val model: String,
    public val input: String,
    public val voice: String,
    @SerialName("response_format")
    public val responseFormat: String? = null,
    public val speed: Double? = null,
)

@Serializable
public data class TranscriptionResponse(
    public val text: String? = null,
    public val language: String? = null,
    public val duration: Double? = null,
    public val words: JsonElement? = null,
    public val segments: JsonElement? = null,
)

public interface AudioClient {
    public suspend fun speech(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>

    public suspend fun transcription(
        audioBytes: ByteArray,
        audioFilename: String,
        model: String,
        prompt: String? = null,
        language: String? = null,
        responseFormat: String? = null,
        temperature: Double? = null,
        contentType: String = "audio/wav",
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>

    public suspend fun translation(
        audioBytes: ByteArray,
        audioFilename: String,
        model: String,
        prompt: String? = null,
        responseFormat: String? = null,
        temperature: Double? = null,
        contentType: String = "audio/wav",
        headers: Map<String, String> = emptyMap(),
    ): OpenAIResult<String>
}

internal class AudioClientImpl(private val client: OpenAIClient) : AudioClient {
    override suspend fun speech(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("audio/speech", body, headers)

    override suspend fun transcription(
        audioBytes: ByteArray,
        audioFilename: String,
        model: String,
        prompt: String?,
        language: String?,
        responseFormat: String?,
        temperature: Double?,
        contentType: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.postMultipart(
        endpoint = "audio/transcriptions",
        parts = buildList {
            add(MultipartPart.Binary("file", audioBytes, audioFilename, contentType))
            add(MultipartPart.Text("model", model))
            prompt?.let { add(MultipartPart.Text("prompt", it)) }
            language?.let { add(MultipartPart.Text("language", it)) }
            responseFormat?.let { add(MultipartPart.Text("response_format", it)) }
            temperature?.let { add(MultipartPart.Text("temperature", it.toString())) }
        },
        headers = headers,
    )

    override suspend fun translation(
        audioBytes: ByteArray,
        audioFilename: String,
        model: String,
        prompt: String?,
        responseFormat: String?,
        temperature: Double?,
        contentType: String,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.postMultipart(
        endpoint = "audio/translations",
        parts = buildList {
            add(MultipartPart.Binary("file", audioBytes, audioFilename, contentType))
            add(MultipartPart.Text("model", model))
            prompt?.let { add(MultipartPart.Text("prompt", it)) }
            responseFormat?.let { add(MultipartPart.Text("response_format", it)) }
            temperature?.let { add(MultipartPart.Text("temperature", it.toString())) }
        },
        headers = headers,
    )
}

public fun OpenAIClient.audio(): AudioClient = AudioClientImpl(this)

public suspend fun AudioClient.speechRaw(
    request: SpeechRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<String> = speech(request.toJsonBody(defaultJson), headers)

public suspend fun AudioClient.transcriptionTyped(
    audioBytes: ByteArray,
    audioFilename: String,
    model: String,
    prompt: String? = null,
    language: String? = null,
    responseFormat: String? = null,
    temperature: Double? = null,
    contentType: String = "audio/wav",
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<TranscriptionResponse> = transcription(
    audioBytes = audioBytes,
    audioFilename = audioFilename,
    model = model,
    prompt = prompt,
    language = language,
    responseFormat = responseFormat,
    temperature = temperature,
    contentType = contentType,
    headers = headers,
).deserialize()

public suspend fun AudioClient.translationTyped(
    audioBytes: ByteArray,
    audioFilename: String,
    model: String,
    prompt: String? = null,
    responseFormat: String? = null,
    temperature: Double? = null,
    contentType: String = "audio/wav",
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<TranscriptionResponse> = translation(
    audioBytes = audioBytes,
    audioFilename = audioFilename,
    model = model,
    prompt = prompt,
    responseFormat = responseFormat,
    temperature = temperature,
    contentType = contentType,
    headers = headers,
).deserialize()
