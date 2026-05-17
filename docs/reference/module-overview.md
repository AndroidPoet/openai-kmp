# Module Overview

`openai-kmp` is split by API domains so apps can keep dependency footprint minimal.

- Include only `openai-client` + selected feature modules.
- Each feature module offers both raw (`String`) and typed helpers.
- Multipart-heavy APIs (`files`, `uploads`, `audio`, `images`) are handled in feature modules while transport stays centralized in `openai-client`.

## Core Layers

1. `openai-core`
   - Shared error payload model.
   - Shared list envelope model.
   - Shared success/failure wrapper utilities.
2. `openai-client`
   - Ktor-backed transport with platform engine selection.
   - Global headers (`Authorization`, `OpenAI-Organization`, `OpenAI-Project`).
   - JSON and multipart request handling.
3. Feature modules
   - Domain clients mapped to OpenAI endpoint groups.
   - Typed request/response data classes where stable.
   - Escape hatches via `JsonElement` for evolving payload areas.

## Feature Modules

- `openai-responses`
  - `POST /responses`
  - `GET /responses/{id}`
  - `DELETE /responses/{id}`
  - `POST /responses/{id}/cancel`
  - `POST /responses/compact`
  - `GET /responses/{id}/input_items`
  - `POST /responses/input_tokens`
- `openai-chat`
  - `POST /chat/completions`
  - `GET/POST/DELETE /chat/completions/{id}`
  - `GET /chat/completions/{id}/messages`
- `openai-embeddings`
  - `POST /embeddings`
- `openai-files`
  - multipart file upload and file CRUD/content
- `openai-batches`
  - batch create/retrieve/list/cancel
- `openai-models`
  - model retrieve/list/delete
- `openai-moderations`
  - moderation create
- `openai-images`
  - generations, edits, variations
- `openai-audio`
  - speech, transcription, translation
- `openai-finetuning`
  - jobs + events + checkpoints
- `openai-vectorstores`
  - vector store CRUD/search + file operations
- `openai-uploads`
  - upload lifecycle + part upload
