package io.github.androidpoet.openai.client

import io.ktor.client.plugins.logging.LogLevel

@DslMarker
public annotation class OpenAIDsl

@OpenAIDsl
public class OpenAIConfigBuilder {
    public var logging: Boolean = false
    public var logLevel: LogLevel = LogLevel.NONE
    public var organization: String? = null
    public var project: String? = null
    public var timeoutMillis: Long = 60_000
    public val headers: MutableMap<String, String> = mutableMapOf()

    internal fun build(): OpenAIConfig = OpenAIConfig(
        logging = logging,
        logLevel = logLevel,
        organization = organization,
        project = project,
        timeoutMillis = timeoutMillis,
        headers = headers.toMap(),
    )
}

public data class OpenAIConfig(
    val logging: Boolean,
    val logLevel: LogLevel,
    val organization: String?,
    val project: String?,
    val timeoutMillis: Long,
    val headers: Map<String, String>,
)
