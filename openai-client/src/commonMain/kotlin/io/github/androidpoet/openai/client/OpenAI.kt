package io.github.androidpoet.openai.client

import io.github.androidpoet.openai.client.transport.HttpTransport
import io.github.androidpoet.openai.client.transport.platformEngine

public object OpenAI {
    public fun create(
        apiKey: String,
        baseUrl: String = "https://api.openai.com/v1",
        configure: OpenAIConfigBuilder.() -> Unit = {},
    ): OpenAIClient {
        val config = OpenAIConfigBuilder().apply(configure).build()
        val transport = HttpTransport(
            config = config,
            engineFactory = platformEngine(),
            baseUrl = baseUrl,
            apiKey = apiKey,
        )
        return OpenAIClientImpl(
            baseUrl = baseUrl,
            apiKey = apiKey,
            transport = transport,
        )
    }
}
