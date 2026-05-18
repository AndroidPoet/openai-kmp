package io.github.androidpoet.openai.core

import io.github.androidpoet.openai.core.result.OpenAIError
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.core.result.getOrElse
import io.github.androidpoet.openai.core.result.map
import io.github.androidpoet.openai.core.result.onRateLimited
import io.github.androidpoet.openai.core.result.onUnauthorized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAIResultTest {
    @Test
    fun test_openAIResult_map_whenSuccess_returnsTransformedValue() {
        val result = OpenAIResult.Success(3).map { it * 2 }

        assertTrue(result is OpenAIResult.Success)
        assertEquals(6, (result as OpenAIResult.Success).value)
    }

    @Test
    fun test_openAIResult_getOrElse_whenFailure_returnsFallbackValue() {
        val result: OpenAIResult<Int> = OpenAIResult.Failure(OpenAIError("boom"))

        val fallback = result.getOrElse { -1 }

        assertEquals(-1, fallback)
    }

    @Test
    fun test_openAIResult_onUnauthorized_runsHandlerForUnauthorizedCategory() {
        var called = false
        val result: OpenAIResult<Int> = OpenAIResult.Failure(OpenAIError("nope", statusCode = 401))

        result.onUnauthorized { called = true }

        assertTrue(called)
    }

    @Test
    fun test_openAIResult_onRateLimited_runsHandlerForRateLimitCategory() {
        var called = false
        val result: OpenAIResult<Int> = OpenAIResult.Failure(OpenAIError("slow down", statusCode = 429))

        result.onRateLimited { called = true }

        assertTrue(called)
    }
}
