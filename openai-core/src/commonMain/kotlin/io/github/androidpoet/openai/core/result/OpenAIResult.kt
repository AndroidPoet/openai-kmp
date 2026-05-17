package io.github.androidpoet.openai.core.result

public sealed interface OpenAIResult<out T> {
    public data class Success<out T>(public val value: T) : OpenAIResult<T>

    public data class Failure(public val error: OpenAIError) : OpenAIResult<Nothing>

    public val isSuccess: Boolean get() = this is Success

    public val isFailure: Boolean get() = this is Failure

    public fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    public fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error.toException()
    }

    public fun errorOrNull(): OpenAIError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    public companion object {
        public inline fun <T> catching(block: () -> T): OpenAIResult<T> =
            try {
                Success(block())
            } catch (e: OpenAIException) {
                Failure(e.error)
            } catch (e: Exception) {
                Failure(OpenAIError(message = e.message ?: "Unknown error"))
            }
    }
}

public inline fun <T, R> OpenAIResult<T>.map(transform: (T) -> R): OpenAIResult<R> = when (this) {
    is OpenAIResult.Success -> OpenAIResult.Success(transform(value))
    is OpenAIResult.Failure -> this
}

public inline fun <T, R> OpenAIResult<T>.flatMap(
    transform: (T) -> OpenAIResult<R>,
): OpenAIResult<R> = when (this) {
    is OpenAIResult.Success -> transform(value)
    is OpenAIResult.Failure -> this
}

/** Runs [action] only when the outcome is successful and returns the same outcome for chaining. */
public inline fun <T> OpenAIResult<T>.onSuccess(action: (T) -> Unit): OpenAIResult<T> = apply {
    if (this is OpenAIResult.Success) action(value)
}

/** Runs [action] only when the outcome failed and returns the same outcome for chaining. */
public inline fun <T> OpenAIResult<T>.onFailure(action: (OpenAIError) -> Unit): OpenAIResult<T> = apply {
    if (this is OpenAIResult.Failure) action(error)
}

public inline fun <T> OpenAIResult<T>.onFailureCategory(
    category: OpenAIErrorCategory,
    action: (OpenAIError) -> Unit,
): OpenAIResult<T> = apply {
    if (this is OpenAIResult.Failure && error.category == category) action(error)
}

public inline fun <T> OpenAIResult<T>.recover(transform: (OpenAIError) -> T): OpenAIResult<T> = when (this) {
    is OpenAIResult.Success -> this
    is OpenAIResult.Failure -> OpenAIResult.Success(transform(error))
}

public inline fun <T> OpenAIResult<T>.getOrElse(defaultValue: (OpenAIError) -> T): T = when (this) {
    is OpenAIResult.Success -> value
    is OpenAIResult.Failure -> defaultValue(error)
}
