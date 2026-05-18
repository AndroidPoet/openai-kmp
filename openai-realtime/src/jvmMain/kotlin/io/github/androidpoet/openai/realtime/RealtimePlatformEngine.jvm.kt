package io.github.androidpoet.openai.realtime

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun realtimePlatformEngine(): HttpClientEngineFactory<*> = OkHttp
