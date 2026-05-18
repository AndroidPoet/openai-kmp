package io.github.androidpoet.openai.realtime

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

internal actual fun realtimePlatformEngine(): HttpClientEngineFactory<*> = Darwin
