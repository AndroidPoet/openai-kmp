# Integration Patterns

This guide focuses on production integration patterns for large apps.

## 1) Single Transport Instance Pattern

Create one client instance and reuse it:

```kotlin
class OpenAIGateway(apiKey: String) {
    private val client = OpenAI.create(apiKey) {
        timeoutMillis = 90_000
    }

    val responses = client.responses()
    val files = client.files()
    val embeddings = client.embeddings()

    fun close() = client.close()
}
```

Why:
- shared connection pools
- consistent auth headers
- lower allocation churn

## 2) Domain Facade Pattern

Wrap module clients behind your own domain service:

```kotlin
class SupportReplyService(private val responses: ResponsesClient) {
    suspend fun draftReply(input: JsonElement): String? {
        val outcome = responses.createTyped(
            ResponseCreateRequest(
                model = "gpt-4.1-mini",
                input = input,
            ),
        )
        return outcome.getOrNull()?.id
    }
}
```

Why:
- app code depends on business semantics, not endpoint details
- easier testing with fake services

## 3) Endpoint-Retry Policy Pattern

Use retry policy by endpoint category:

1. idempotent reads (`retrieve`, `list`)  
   - retry on network + 429 + 5xx
2. mutating writes (`create`, `delete`, `cancel`)  
   - retry conservatively, prefer idempotency keys when available
3. multipart uploads  
   - retry with checksum guards and part-level reconciliation

## 4) Structured Logging Pattern

Log a stable envelope:

- endpoint family (`responses`, `files`, `vector_stores`)
- method (`create`, `list`, `delete`)
- status code
- latency
- response id / file id / job id where available

Avoid logging:
- raw user prompts
- full uploaded binary content
- API tokens

## 5) Multi-Module Adoption Pattern

Start small:

1. `openai-client`
2. one feature module needed for your first use case
3. add modules incrementally

This keeps app footprint smaller than importing all modules up front.

## 6) Raw-Then-Typed Rollout Pattern

For fast-moving APIs:

1. first integrate raw call methods with app-owned JSON parsing
2. once payload stabilizes, migrate call sites to typed helpers
3. keep raw fallback path for advanced or beta fields

## 7) Coroutine Boundary Pattern

Define clear coroutine boundaries:

- call SDK only from IO-focused dispatcher context in app layer
- map SDK outcomes into app-specific sealed outcomes
- expose UI-layer friendly state (`Loading`, `Data`, `Error`)

## 8) File + Vector Store Pipeline Pattern

Typical retrieval pipeline:

1. upload file (`files.create`)
2. create vector store (`vectorStores.createTyped`)
3. attach file (`vectorStores.addFile`)
4. query store (`vectorStores.searchTyped`)

Recommended metadata:
- tenant id
- document source
- version tag

## 9) Batch Processing Pattern

For large offline workloads:

1. generate JSONL request file
2. upload as file with batch purpose
3. create batch
4. poll by `retrieve`
5. download output file on completion

## 10) Safe Cleanup Pattern

Build cleanup jobs around:

- expired uploads
- old vector stores
- transient files
- canceled fine-tuning jobs

Keep cleanup idempotent and paginated.

## 11) Error Normalization Pattern

Convert SDK errors to app error taxonomy:

- auth/session issue
- quota/rate issue
- validation issue
- dependency outage
- unknown internal issue

This keeps product behavior predictable across features.

## 12) Testing Pattern

Recommended test layers:

1. unit tests for domain services using fake clients
2. integration tests against mocked HTTP gateway
3. canary environment tests for key endpoints in CI

Validate:
- status code handling
- serialization of request bodies
- multipart composition for file/audio/image flows

## 13) Platform-Specific Guardrails

Android/JVM:
- tune timeout by network quality class

Apple:
- monitor background networking behavior for long-running uploads

Native Linux/Windows:
- verify runtime TLS and proxy environment behavior in deployment targets

WasmJs:
- ensure CORS and browser limits are accounted for when calling via frontend runtime

## 14) Operational Readiness Checklist

- [ ] API key source is secure and rotated
- [ ] endpoint-level metrics captured
- [ ] retry and timeout policy defined
- [ ] file lifecycle and retention policy defined
- [ ] fallback behavior for 429/5xx defined
- [ ] load test done for peak scenarios
