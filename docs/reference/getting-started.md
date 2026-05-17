# Getting Started

## 1) Add Dependencies

```kotlin
[versions]
openai-kmp = "0.0.1"

[libraries]
openai-client = { module = "io.github.androidpoet:openai-client", version.ref = "openai-kmp" }
openai-responses = { module = "io.github.androidpoet:openai-responses", version.ref = "openai-kmp" }
openai-files = { module = "io.github.androidpoet:openai-files", version.ref = "openai-kmp" }
openai-embeddings = { module = "io.github.androidpoet:openai-embeddings", version.ref = "openai-kmp" }
```

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.openai.client)
            implementation(libs.openai.responses)
            implementation(libs.openai.files)
            implementation(libs.openai.embeddings)
        }
    }
}
```

## 2) Create a Client

```kotlin
val client = OpenAI.create(
    apiKey = System.getenv("OPENAI_API_KEY"),
) {
    logging = true
    timeoutMillis = 60_000
}
```

## 3) Make a Request

```kotlin
val responses = client.responses()
val outcome = responses.createTyped(
    ResponseCreateRequest(
        model = "gpt-4.1-mini",
        input = buildJsonArray {
            addJsonObject {
                put("role", "user")
                put("content", buildJsonArray {
                    addJsonObject {
                        put("type", "input_text")
                        put("text", "Write a short Kotlin tip.")
                    }
                })
            }
        },
    ),
)
```

## 4) Close the Client

```kotlin
client.close()
```

## Recommended Structure in Apps

1. Create one shared `OpenAIClient` per app process/session.
2. Expose feature clients (`responses()`, `files()`, `embeddings()`) through your DI container.
3. Keep request body builders close to domain logic rather than inside UI code.
