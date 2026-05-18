package io.github.androidpoet.openai.realtime

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun realtimePlatformEngine(): HttpClientEngineFactory<*>
