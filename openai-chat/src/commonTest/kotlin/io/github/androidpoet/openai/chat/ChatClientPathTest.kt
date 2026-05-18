package io.github.androidpoet.openai.chat

import io.github.androidpoet.openai.client.MultipartPart
import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.core.result.OpenAIResult
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatClientPathTest {
    @Test
    fun test_retrieve_encodesCompletionId() {
        val fake = FakeOpenAIClient()
        val chat = fake.chat()

        runSuspend { chat.retrieve("id/with space") }

        assertEquals("chat/completions/id%2Fwith%20space", fake.lastGetEndpoint)
    }
}

private class FakeOpenAIClient : OpenAIClient {
    override val baseUrl: String = "https://api.openai.com/v1"
    override val apiKey: String = "test"

    var lastGetEndpoint: String? = null

    override suspend fun get(endpoint: String, queryParams: List<Pair<String, String>>, headers: Map<String, String>): OpenAIResult<String> {
        lastGetEndpoint = endpoint
        return OpenAIResult.Success("{}")
    }

    override suspend fun post(endpoint: String, body: String?, headers: Map<String, String>): OpenAIResult<String> = OpenAIResult.Success("{}")
    override suspend fun patch(endpoint: String, body: String?, headers: Map<String, String>): OpenAIResult<String> = OpenAIResult.Success("{}")
    override suspend fun put(endpoint: String, body: String?, headers: Map<String, String>): OpenAIResult<String> = OpenAIResult.Success("{}")
    override suspend fun delete(endpoint: String, body: String?, headers: Map<String, String>): OpenAIResult<String> = OpenAIResult.Success("{}")
    override suspend fun postMultipart(endpoint: String, parts: List<MultipartPart>, headers: Map<String, String>): OpenAIResult<String> = OpenAIResult.Success("{}")
    override fun close() = Unit
}

private fun <T> runSuspend(block: suspend () -> T): T = kotlinx.coroutines.runBlocking { block() }
