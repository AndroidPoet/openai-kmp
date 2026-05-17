# Configuration Reference

`OpenAI.create` accepts a config builder with transport and header options.

## Fields

- `logging: Boolean`
  - Enables Ktor logging plugin.
  - Default: `false`
- `logLevel: LogLevel`
  - Ktor log level (e.g. `NONE`, `BODY`).
  - Default: `NONE`
- `organization: String?`
  - Sent as `OpenAI-Organization` header.
  - Default: `null`
- `project: String?`
  - Sent as `OpenAI-Project` header.
  - Default: `null`
- `timeoutMillis: Long`
  - Request/connect/socket timeout.
  - Default: `60000`
- `headers: MutableMap<String, String>`
  - Additional custom headers.
  - Default: empty

## Example

```kotlin
val client = OpenAI.create(
    apiKey = token,
    baseUrl = "https://api.openai.com/v1",
) {
    logging = true
    logLevel = LogLevel.HEADERS
    organization = "org_123"
    project = "proj_123"
    timeoutMillis = 90_000
    headers["X-App-Name"] = "my-kmp-app"
}
```

## Base URL Notes

- Default: `https://api.openai.com/v1`
- You can override for gateway/proxy setups.
- Endpoint methods use path fragments and resolve against this base URL.
