# OpenAI KMP

Kotlin Multiplatform SDK for OpenAI APIs with the same modular architecture style used in `supabase-kmp`.

## Modules

- `openai-core` — shared error model and common list model
- `openai-client` — HTTP transport, config, typed serialization helpers
- `openai-responses` — `/responses`, `/responses/{id}`, `/responses/compact`, input items/tokens
- `openai-chat` — `/chat/completions` + completion message listing
- `openai-embeddings` — `/embeddings`
- `openai-files` — `/files` CRUD + content
- `openai-batches` — `/batches`
- `openai-models` — `/models`
- `openai-moderations` — `/moderations`
- `openai-images` — image generation/edits/variations
- `openai-audio` — speech, transcriptions, translations
- `openai-finetuning` — fine-tuning jobs, events, checkpoints
- `openai-vectorstores` — vector store CRUD/search + file operations
- `openai-uploads` — multipart upload session APIs

## Setup

```kotlin
[versions]
openai-kmp = "0.1.0"

[libraries]
openai-client = { module = "io.github.androidpoet:openai-client", version.ref = "openai-kmp" }
openai-responses = { module = "io.github.androidpoet:openai-responses", version.ref = "openai-kmp" }
openai-files = { module = "io.github.androidpoet:openai-files", version.ref = "openai-kmp" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.openai.client)
            implementation(libs.openai.responses)
            implementation(libs.openai.files)
        }
    }
}
```

## Quick Start

```kotlin
val client = OpenAI.create(apiKey = System.getenv("OPENAI_API_KEY")) {
    logging = true
}

val responses = client.responses()
val responseOutcome = responses.createTyped(
    request = ResponseCreateRequest(
        model = "gpt-4.1-mini",
        input = buildJsonArray {
            addJsonObject {
                put("role", "user")
                put("content", buildJsonArray {
                    addJsonObject {
                        put("type", "input_text")
                        put("text", "Say hello from openai-kmp")
                    }
                })
            }
        },
    ),
)
```
