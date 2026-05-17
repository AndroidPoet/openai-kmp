package io.github.androidpoet.openai.sample

import io.github.androidpoet.openai.client.OpenAI
import io.github.androidpoet.openai.responses.ResponseCreateRequest
import io.github.androidpoet.openai.responses.createTyped
import io.github.androidpoet.openai.responses.responses
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

public suspend fun runBasicSample(apiKey: String): String? {
    val client = OpenAI.create(apiKey)
    val responses = client.responses()
    val result = responses.createTyped(
        ResponseCreateRequest(
            model = "gpt-4.1-mini",
            input = buildJsonArray {
                add(
                    buildJsonObject {
                        put("role", "user")
                        put(
                            "content",
                            buildJsonArray {
                                add(
                                    buildJsonObject {
                                        put("type", "input_text")
                                        put("text", "Return one short sentence saying openai-kmp is online.")
                                    },
                                )
                            },
                        )
                    },
                )
            },
        ),
    )
    client.close()
    return result.getOrNull()?.id
}
