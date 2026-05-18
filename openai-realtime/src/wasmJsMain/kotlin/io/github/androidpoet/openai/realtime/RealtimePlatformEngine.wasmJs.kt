package io.github.androidpoet.openai.realtime

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

internal actual fun realtimePlatformEngine(): HttpClientEngineFactory<*> = Js
