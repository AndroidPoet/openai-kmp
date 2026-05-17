package io.github.androidpoet.openai.core.result

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class OpenAIError(
    public val message: String,
    public val code: String? = null,
    public val type: String? = null,
    public val param: String? = null,
    public val details: JsonElement? = null,
    public val statusCode: Int? = null,
)

public class OpenAIException(public val error: OpenAIError) : Exception(error.message)

public fun OpenAIError.toException(): OpenAIException = OpenAIException(this)

public enum class OpenAIErrorCategory {
    Unauthorized,
    NotFound,
    RateLimited,
    Validation,
    Conflict,
    Server,
    Unknown,
}

public val OpenAIError.category: OpenAIErrorCategory
    get() = when {
        statusCode == 401 || statusCode == 403 -> OpenAIErrorCategory.Unauthorized
        statusCode == 404 -> OpenAIErrorCategory.NotFound
        statusCode == 409 -> OpenAIErrorCategory.Conflict
        statusCode == 422 || statusCode == 400 -> OpenAIErrorCategory.Validation
        statusCode == 429 -> OpenAIErrorCategory.RateLimited
        statusCode != null && statusCode >= 500 -> OpenAIErrorCategory.Server
        else -> when (code) {
            "invalid_api_key" -> OpenAIErrorCategory.Unauthorized
            "rate_limit_exceeded" -> OpenAIErrorCategory.RateLimited
            "not_found_error" -> OpenAIErrorCategory.NotFound
            else -> OpenAIErrorCategory.Unknown
        }
    }
