package io.github.androidpoet.openai.realtime

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

internal actual fun realtimePlatformEngine(): HttpClientEngineFactory<*> = CIO
