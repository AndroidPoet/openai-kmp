# Platform Engine Matrix

`openai-client` uses Ktor with per-platform engines selected via `expect/actual`.

| Target | Engine |
|---|---|
| Android | OkHttp |
| JVM | OkHttp |
| iOS/macOS/tvOS/watchOS | Darwin |
| Linux x64 | CIO |
| Windows mingwX64 | CIO |
| WasmJs | Js |

## Why This Split

- OkHttp provides reliable behavior on Android/JVM.
- Darwin is the native Apple integration path in Ktor.
- CIO works well for Linux and Windows native targets.
- Js engine is required for wasm/js networking.

## Operational Notes

- Timeout config is centralized in `OpenAIConfig.timeoutMillis`.
- Auth and custom headers are injected in a shared default request block.
- Multipart requests are assembled in transport and reused by feature modules.
