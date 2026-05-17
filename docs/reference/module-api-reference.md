# Module API Reference

This page maps current SDK module APIs to endpoint groups and exposed client methods.

## `openai-client`

Core transport methods:
- `get(endpoint, queryParams, headers)`
- `post(endpoint, body, headers)`
- `patch(endpoint, body, headers)`
- `put(endpoint, body, headers)`
- `delete(endpoint, body, headers)`
- `postMultipart(endpoint, parts, headers)`

Configuration:
- logging toggle and log level
- org/project headers
- request/connect/socket timeout
- custom header map

---

## `openai-responses`

Factory:
- `OpenAIClient.responses()`

Raw methods:
- `create(body, headers)` -> `POST /responses`
- `retrieve(responseId, headers)` -> `GET /responses/{responseId}`
- `delete(responseId, headers)` -> `DELETE /responses/{responseId}`
- `cancel(responseId, headers)` -> `POST /responses/{responseId}/cancel`
- `compact(body, headers)` -> `POST /responses/compact`
- `inputItems(responseId, headers)` -> `GET /responses/{responseId}/input_items`
- `inputTokens(body, headers)` -> `POST /responses/input_tokens`

Typed helpers:
- `createTyped(request, headers)`
- `retrieveTyped(responseId, headers)`
- `compactTyped(request, headers)`
- `inputItemsTyped(responseId, headers)`
- `inputTokensTyped(body, headers)`

Request models:
- `ResponseCreateRequest`
- `ResponseCompactRequest`

Response models:
- `OpenAIResponseObject`
- `ResponseInputTokenObject`

---

## `openai-chat`

Factory:
- `OpenAIClient.chat()`

Raw methods:
- `create(body, headers)` -> `POST /chat/completions`
- `retrieve(completionId, headers)` -> `GET /chat/completions/{completionId}`
- `update(completionId, body, headers)` -> `POST /chat/completions/{completionId}`
- `list(queryParams, headers)` -> `GET /chat/completions`
- `delete(completionId, headers)` -> `DELETE /chat/completions/{completionId}`
- `messages(completionId, headers)` -> `GET /chat/completions/{completionId}/messages`

Typed helpers:
- `createTyped(request, headers)`
- `retrieveTyped(completionId, headers)`
- `listTyped(queryParams, headers)`
- `messagesTyped(completionId, headers)`

Request models:
- `ChatCompletionRequest`

Response models:
- `ChatCompletionObject`

---

## `openai-embeddings`

Factory:
- `OpenAIClient.embeddings()`

Raw methods:
- `create(body, headers)` -> `POST /embeddings`

Typed helpers:
- `createTyped(request, headers)`

Request models:
- `EmbeddingRequest`

Response models:
- `EmbeddingResponse`
- `EmbeddingData`

---

## `openai-files`

Factory:
- `OpenAIClient.files()`

Raw methods:
- `create(purpose, bytes, filename, contentType, headers)` -> `POST /files` (multipart)
- `retrieve(fileId, headers)` -> `GET /files/{fileId}`
- `list(queryParams, headers)` -> `GET /files`
- `delete(fileId, body, headers)` -> `DELETE /files/{fileId}`
- `content(fileId, headers)` -> `GET /files/{fileId}/content`

Typed helpers:
- `retrieveTyped(fileId, headers)`
- `listTyped(queryParams, headers)`
- `deleteTyped(fileId, body, headers)`
- `contentJsonTyped(fileId, headers)`

Request models:
- `FileDeleteBody`

Response models:
- `OpenAIFileObject`
- `OpenAIFileDeleted`

---

## `openai-batches`

Factory:
- `OpenAIClient.batches()`

Raw methods:
- `create(body, headers)` -> `POST /batches`
- `retrieve(batchId, headers)` -> `GET /batches/{batchId}`
- `list(queryParams, headers)` -> `GET /batches`
- `cancel(batchId, headers)` -> `POST /batches/{batchId}/cancel`

Typed helpers:
- `createTyped(request, headers)`
- `retrieveTyped(batchId, headers)`
- `listTyped(queryParams, headers)`

Request models:
- `BatchCreateRequest`

Response models:
- `BatchObject`

---

## `openai-models`

Factory:
- `OpenAIClient.models()`

Raw methods:
- `retrieve(model, headers)` -> `GET /models/{model}`
- `list(queryParams, headers)` -> `GET /models`
- `delete(model, headers)` -> `DELETE /models/{model}`

Typed helpers:
- `retrieveTyped(model, headers)`
- `listTyped(queryParams, headers)`
- `deleteTyped(model, headers)`

Response models:
- `ModelObject`
- `ModelDeleted`

---

## `openai-moderations`

Factory:
- `OpenAIClient.moderations()`

Raw methods:
- `create(body, headers)` -> `POST /moderations`

Typed helpers:
- `createTyped(request, headers)`

Request models:
- `ModerationRequest`

Response models:
- `ModerationResponse`

---

## `openai-images`

Factory:
- `OpenAIClient.images()`

Raw methods:
- `generate(body, headers)` -> `POST /images/generations`
- `edit(...)` -> `POST /images/edits` (multipart)
- `variation(...)` -> `POST /images/variations` (multipart)

Typed helpers:
- `generateTyped(request, headers)`

Request models:
- `ImageGenerationRequest`

Response models:
- `ImageGenerationResponse`

---

## `openai-audio`

Factory:
- `OpenAIClient.audio()`

Raw methods:
- `speech(body, headers)` -> `POST /audio/speech`
- `transcription(...)` -> `POST /audio/transcriptions` (multipart)
- `translation(...)` -> `POST /audio/translations` (multipart)

Typed helpers:
- `speechRaw(request, headers)`
- `transcriptionTyped(...)`
- `translationTyped(...)`

Request models:
- `SpeechRequest`

Response models:
- `TranscriptionResponse`

---

## `openai-finetuning`

Factory:
- `OpenAIClient.fineTuning()`

Raw methods:
- `createJob(body, headers)` -> `POST /fine_tuning/jobs`
- `retrieveJob(jobId, headers)` -> `GET /fine_tuning/jobs/{jobId}`
- `listJobs(queryParams, headers)` -> `GET /fine_tuning/jobs`
- `cancelJob(jobId, headers)` -> `POST /fine_tuning/jobs/{jobId}/cancel`
- `events(jobId, queryParams, headers)` -> `GET /fine_tuning/jobs/{jobId}/events`
- `pause(jobId, headers)` -> `POST /fine_tuning/jobs/{jobId}/pause`
- `resume(jobId, headers)` -> `POST /fine_tuning/jobs/{jobId}/resume`
- `checkpoints(jobId, queryParams, headers)` -> `GET /fine_tuning/jobs/{jobId}/checkpoints`

Typed helpers:
- `createJobTyped(request, headers)`
- `retrieveJobTyped(jobId, headers)`
- `listJobsTyped(queryParams, headers)`
- `eventsTyped(jobId, queryParams, headers)`
- `checkpointsTyped(jobId, queryParams, headers)`

Request models:
- `FineTuningJobCreateRequest`

Response models:
- `FineTuningJobObject`

---

## `openai-vectorstores`

Factory:
- `OpenAIClient.vectorStores()`

Raw methods:
- `create(body, headers)` -> `POST /vector_stores`
- `retrieve(vectorStoreId, headers)` -> `GET /vector_stores/{vectorStoreId}`
- `update(vectorStoreId, body, headers)` -> `POST /vector_stores/{vectorStoreId}`
- `list(queryParams, headers)` -> `GET /vector_stores`
- `delete(vectorStoreId, headers)` -> `DELETE /vector_stores/{vectorStoreId}`
- `search(vectorStoreId, body, headers)` -> `POST /vector_stores/{vectorStoreId}/search`
- `addFile(vectorStoreId, body, headers)` -> `POST /vector_stores/{vectorStoreId}/files`
- `retrieveFile(vectorStoreId, fileId, headers)` -> `GET /vector_stores/{vectorStoreId}/files/{fileId}`
- `listFiles(vectorStoreId, queryParams, headers)` -> `GET /vector_stores/{vectorStoreId}/files`
- `deleteFile(vectorStoreId, fileId, headers)` -> `DELETE /vector_stores/{vectorStoreId}/files/{fileId}`

Typed helpers:
- `createTyped(request, headers)`
- `retrieveTyped(vectorStoreId, headers)`
- `listTyped(queryParams, headers)`
- `searchTyped(vectorStoreId, request, headers)`
- `listFilesTyped(vectorStoreId, queryParams, headers)`

Request models:
- `VectorStoreCreateRequest`
- `VectorStoreSearchRequest`

Response models:
- `VectorStoreObject`

---

## `openai-uploads`

Factory:
- `OpenAIClient.uploads()`

Raw methods:
- `create(body, headers)` -> `POST /uploads`
- `cancel(uploadId, headers)` -> `POST /uploads/{uploadId}/cancel`
- `complete(uploadId, body, headers)` -> `POST /uploads/{uploadId}/complete`
- `addPart(uploadId, bytes, filename, contentType, headers)` -> `POST /uploads/{uploadId}/parts` (multipart)

Typed helpers:
- `createTyped(request, headers)`
- `completeTyped(uploadId, request, headers)`
- `cancelTyped(uploadId, headers)`

Request models:
- `UploadCreateRequest`
- `UploadCompleteRequest`

Response models:
- `UploadObject`

---

## Shared Modeling Notes

1. List endpoints generally decode into `OpenAIListResponse<T>`.
2. Some fields remain `JsonElement` intentionally for compatibility with evolving API payloads.
3. All modules provide typed helpers without blocking raw usage paths.
