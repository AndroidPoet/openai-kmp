package io.github.androidpoet.openai.client

import io.github.androidpoet.openai.core.result.OpenAIError
import io.github.androidpoet.openai.core.result.OpenAIResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public val defaultJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
}

public inline fun <reified T> T.toJsonBody(json: Json = defaultJson): String = json.encodeToString(this)

public suspend inline fun <reified T> OpenAIClient.getTyped(
    endpoint: String,
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<T> = get(endpoint, queryParams, headers).deserialize()

public suspend inline fun <reified T> OpenAIClient.postTyped(
    endpoint: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<T> = post(endpoint, body, headers).deserialize()

public suspend inline fun <reified T> OpenAIClient.patchTyped(
    endpoint: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<T> = patch(endpoint, body, headers).deserialize()

public suspend inline fun <reified T> OpenAIClient.putTyped(
    endpoint: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<T> = put(endpoint, body, headers).deserialize()

public suspend inline fun <reified T> OpenAIClient.deleteTyped(
    endpoint: String,
    body: String? = null,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<T> = delete(endpoint, body, headers).deserialize()

public inline fun <reified T> OpenAIResult<String>.deserialize(
    json: Json = defaultJson,
): OpenAIResult<T> = when (this) {
    is OpenAIResult.Success -> try {
        OpenAIResult.Success(json.decodeFromString<T>(value))
    } catch (e: SerializationException) {
        OpenAIResult.Failure(OpenAIError(message = "Deserialization failed: ${e.message}"))
    } catch (e: IllegalArgumentException) {
        OpenAIResult.Failure(OpenAIError(message = "Invalid payload: ${e.message}"))
    }

    is OpenAIResult.Failure -> this
}
