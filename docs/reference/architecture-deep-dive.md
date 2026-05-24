# Architecture Deep Dive

This SDK is organized to keep platform transport, endpoint wiring, and domain payload models isolated from each other.

## Design Goals

1. Keep each OpenAI API area independently consumable.
2. Keep transport behavior consistent across platforms.
3. Keep payload handling flexible enough for fast-moving API fields.
4. Keep call sites ergonomic with typed helpers and raw escape hatches.

## Layering Model

## Layer 1: `openai-core`

Responsibilities:
- shared error payload model and category mapping
- shared operation outcome wrapper helpers
- generic list envelope model used by list endpoints

Why this layer exists:
- feature modules avoid re-implementing error contracts
- app code can handle success/failure with one shared API style

## Layer 2: `openai-client`

Responsibilities:
- Ktor `HttpClient` setup with platform-specific engine
- default headers:
  - `Authorization: Bearer ...`
  - `OpenAI-Organization` (optional)
  - `OpenAI-Project` (optional)
  - `X-Client-Info`
- JSON serialization config
- timeout and logging config
- multipart request support used by files/images/audio/uploads

Design choices:
- endpoint methods in feature modules pass path fragments (`responses`, `files/{id}`).
- transport normalizes relative and absolute URLs.
- failure payloads are parsed from OpenAI error envelopes when possible.

## Layer 3: Feature Modules

Each feature module follows one pattern:

1. interface with raw methods (body as `String`, response as `String`)
2. implementation that delegates to `OpenAIClient`
3. typed extensions that encode/decode typed request/response payloads

Benefits:
- raw methods keep compatibility when schemas change.
- typed helpers keep main paths ergonomic and safer.
- integration teams can progressively type only what they need.

## Request and Response Strategy

## Raw Methods

Use raw methods when:
- endpoint payloads evolve quickly
- your app already owns JSON model parsing
- you need a short path to production rollout

## Typed Helpers

Use typed helpers when:
- models are stable in your integration
- you need IDE hints and compile-time field naming
- your team prefers domain-level DTOs

## Multipart Strategy

Multipart endpoints are centralized by intent:
- `files`: upload training and assistant files
- `images`: edits and variations with image binary parts
- `audio`: transcription and translation file uploads
- `uploads`: session-based multi-part object upload

Each module builds module-specific part sets and delegates actual multipart construction to client transport.

## Error Normalization

Transport maps errors into shared shape with:
- message
- code/type/param when present
- status code

Category mapping is based first on HTTP status:
- 401/403 unauthorized
- 404 not found
- 409 conflict
- 400/422 validation
- 429 rate limited
- 5xx server

Then fallback to selected known OpenAI codes when status is absent.

## Platform Behavior

Transport engines:
- Android/JVM: OkHttp
- Apple: Darwin
- Linux/Windows native: CIO
- WasmJs: Js

Operational implications:
- network behavior is consistent at API surface level
- engine-specific performance tuning remains possible in future with minimal API change

## Why Modules Instead of One Big Artifact

Advantages of modular split:
- reduced dependency surface in apps that only need few APIs
- lower compile time impact for consumers
- easier independent evolution and testing
- easier ownership boundaries in large teams

Tradeoff:
- more artifacts versioned together

## Dependency Graph

All feature modules depend on:
- `openai-core`
- `openai-client`

They do not depend on each other, so adoption can stay selective.

## Source-Set Strategy

Every module uses multiplatform targets:
- Android, JVM
- iOS/macOS/tvOS/watchOS
- Linux x64, mingw x64
- WasmJs

This keeps feature parity across platforms at compile layer.

## Serialization Configuration

JSON parser defaults:
- ignore unknown keys
- lenient mode
- omit explicit nulls

Rationale:
- OpenAI responses can evolve quickly; strict decoding would create unnecessary runtime breakage.

## Extensibility Model

Adding a new API domain follows this template:

1. create `openai-<domain>` module
2. define `<Domain>Client` interface
3. implement `<Domain>ClientImpl` delegating raw HTTP methods
4. add typed request/response models for stable fields
5. add `OpenAIClient.<domain>()` extension
6. add docs and usage examples

This preserves consistency with existing modules.

## Operational Recommendations

1. Use one long-lived `OpenAIClient` per app process.
2. Expose feature clients from a shared composition root rather than creating ad hoc instances.
3. Centralize retry/backoff in your app service layer.
4. Keep endpoint-specific request builders close to domain logic.
5. Add telemetry around status code and latency per endpoint family.

## Stability Envelope

Stable:
- module shape
- transport interface
- endpoint path wiring
- core error category behavior

Flexible:
- typed DTO completeness for fast-evolving payload fields
- payload areas currently represented as `JsonElement`

This balance keeps SDK practical while APIs continue to evolve.
