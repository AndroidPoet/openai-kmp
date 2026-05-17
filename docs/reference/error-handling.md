# Error Handling

The SDK normalizes transport and API errors into a shared error object with:

- `message`
- `code`
- `type`
- `param`
- `statusCode`

## Category Mapping

- Unauthorized: HTTP 401/403
- NotFound: HTTP 404
- Conflict: HTTP 409
- Validation: HTTP 400/422
- RateLimited: HTTP 429
- Server: HTTP 5xx
- Unknown: everything else

## Practical Handling Pattern

```kotlin
val outcome = client.models().retrieveTyped("gpt-4.1-mini")
val data = outcome.getOrNull()
if (data == null) {
    val error = outcome.errorOrNull()
    when (error?.statusCode) {
        401, 403 -> { /* refresh credentials */ }
        429 -> { /* backoff + retry */ }
        else -> { /* app-specific fallback */ }
    }
} else {
    // use model data
}
```

## Retry Guidance

Retry candidates:
- network failures
- HTTP 429
- HTTP 500/502/503/504

Do not blindly retry:
- HTTP 400/401/403/404 unless input or auth state changed.
