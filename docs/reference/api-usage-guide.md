# API Usage Guide

This guide shows common patterns for each feature module.

## Responses

```kotlin
val responses = client.responses()
val outcome = responses.createTyped(
    ResponseCreateRequest(model = "gpt-4.1-mini", input = payload),
)
```

## Chat Completions

```kotlin
val chat = client.chat()
val completion = chat.createTyped(
    ChatCompletionRequest(
        model = "gpt-4.1-mini",
        messages = messagesJson,
    ),
)
```

## Embeddings

```kotlin
val embeddings = client.embeddings()
val vector = embeddings.createTyped(
    EmbeddingRequest(
        model = "text-embedding-3-small",
        input = inputJson,
    ),
)
```

## Files

```kotlin
val files = client.files()
val uploaded = files.create(
    purpose = "assistants",
    bytes = fileBytes,
    filename = "knowledge.pdf",
    contentType = "application/pdf",
)
```

## Images

```kotlin
val images = client.images()
val generated = images.generateTyped(
    ImageGenerationRequest(
        model = "gpt-image-1",
        prompt = "A watercolor cityscape at sunrise",
        size = "1024x1024",
    ),
)
```

## Audio

```kotlin
val audio = client.audio()
val transcript = audio.transcriptionTyped(
    audioBytes = wavBytes,
    audioFilename = "meeting.wav",
    model = "gpt-4o-mini-transcribe",
)
```

## Fine-Tuning

```kotlin
val fineTuning = client.fineTuning()
val job = fineTuning.createJobTyped(
    FineTuningJobCreateRequest(
        model = "gpt-4.1-mini",
        trainingFile = "file_abc123",
    ),
)
```

## Vector Stores

```kotlin
val vectorStores = client.vectorStores()
val store = vectorStores.createTyped(
    VectorStoreCreateRequest(name = "docs-index"),
)
```

## Upload Sessions

```kotlin
val uploads = client.uploads()
val upload = uploads.createTyped(
    UploadCreateRequest(
        purpose = "assistants",
        filename = "archive.zip",
        bytesCount = zipSize,
        mimeType = "application/zip",
    ),
)
```

## Raw vs Typed Methods

- Raw methods return plain response text and are useful for fast adoption with evolving schemas.
- Typed helpers decode into data models for stable fields and easier IDE support.

## Outcome Handling Helpers

Every typed call returns an outcome object that supports fluent handling helpers:

```kotlin
val outcome = client.models().retrieveTyped("gpt-4.1-mini")

outcome
    .onSuccess { model ->
        println("model id = ${model.id}")
    }
    .onFailure { error ->
        println("status=${error.statusCode} message=${error.message}")
    }
```

Also available:
- `map`, `flatMap`
- `recover`, `getOrElse`
- `getOrNull`, `getOrThrow`, `errorOrNull`
