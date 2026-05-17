# Migration Guide

This guide helps teams adopt `openai-kmp` from ad hoc HTTP clients or older internal wrappers.

## Migration Scenarios

1. custom Ktor client + hand-written endpoint calls
2. platform-specific OpenAI integrations with duplicated logic
3. one large internal SDK without module boundaries

## Migration Principles

1. migrate endpoint families incrementally
2. preserve current payload contracts during transition
3. avoid transport rewrites and business logic rewrites in one step

## Step-by-Step Plan

## Step 1: Introduce `openai-client` only

- keep existing service interfaces unchanged
- replace raw HTTP engine with `OpenAIClient` underneath
- keep request/response JSON mapping in existing code

Exit criteria:
- no behavior change in existing integration tests

## Step 2: Adopt one feature module

Pick one bounded domain (for example `responses` or `embeddings`):

- replace custom URL building with module methods
- keep existing response mapping if needed
- then adopt typed helpers for stable models

Exit criteria:
- endpoint family fully migrated
- no direct string URL concatenation for that family

## Step 3: Standardize error handling

- map SDK error payload into your app-level error taxonomy
- remove duplicate error parser utilities
- centralize retry policy by status code category

Exit criteria:
- all OpenAI calls share one error policy path

## Step 4: Migrate multipart endpoints

Replace custom multipart code with:
- `files.create`
- `images.edit` / `images.variation`
- `audio.transcription` / `audio.translation`
- `uploads.addPart`

Exit criteria:
- no module-specific multipart builders outside SDK boundaries

## Step 5: Decommission old wrappers

- remove obsolete endpoint helpers
- remove duplicated DTOs that are no longer used
- lock in lint rule disallowing direct `/v1/...` OpenAI URL strings in app code

Exit criteria:
- old wrapper package empty or deleted

## Compatibility Notes

## JSON Field Flexibility

If your current integration relies on unknown/extra fields:
- keep raw methods initially
- migrate to typed helpers after schema confidence

## Timeout Behavior

Default timeout is shared across request/connect/socket.
If current app had per-endpoint timeout overrides, add wrapper-level policies in your domain services.

## Header Behavior

Headers automatically include auth and optional org/project fields.
If your old integration injected custom tracing headers, use `OpenAIConfig.headers`.

## Suggested Rollout Order by Risk

Low risk:
1. `models`
2. `moderations`
3. `embeddings`

Medium risk:
4. `responses`
5. `chat`
6. `batches`

Higher operational complexity:
7. `files`
8. `images`
9. `audio`
10. `vectorstores`
11. `uploads`
12. `finetuning`

## Validation Matrix

For each migrated endpoint family, validate:

1. auth failures (401/403)
2. missing resource (404)
3. rate limiting (429)
4. server failures (5xx)
5. malformed payload handling
6. idempotent retry behavior where applicable

## Common Migration Pitfalls

1. mixing old and new retry policies for same endpoint
2. keeping duplicate DTOs with near-identical names
3. forgetting to close old and new transport clients in parallel rollout
4. silently swallowing error payload details during adapter conversion
5. no metrics parity check before/after migration

## Cutover Checklist

- [ ] all targeted endpoint families switched
- [ ] old endpoint URL constants removed
- [ ] old multipart utilities removed
- [ ] telemetry parity confirmed
- [ ] latency and error-rate comparison complete
- [ ] rollback plan documented

## Post-Migration Hardening

1. add contract tests for typed helpers
2. add chaos tests for retry/backoff paths
3. add endpoint ownership docs for each domain team
4. add release note discipline for SDK updates in app repos
