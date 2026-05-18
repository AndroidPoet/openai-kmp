package io.github.androidpoet.openai.files

import io.github.androidpoet.openai.client.MultipartPart
import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.core.result.OpenAIResult
import kotlin.test.Test
import kotlin.test.assertEquals

class FilesClientPathTest {
    @Test
    fun test_content_encodesFileId() {
        val fake = FakeOpenAIClient()
        val files = fake.files()

        runSuspend { files.content("id/with space") }

        assertEquals("files/id%2Fwith%20space/content", fake.lastGetEndpoint)
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
