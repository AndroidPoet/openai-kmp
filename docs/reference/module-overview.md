# Module Overview

`openai-kmp` is split by API domains so apps can keep dependency footprint minimal.

- Include only `openai-client` + selected feature modules.
- Each feature module returns `OpenAIResult<T>` and offers both raw (`String`) and typed helpers.
- Multipart-heavy APIs (`files`, `uploads`, `audio`, `images`) are handled in feature modules while transport stays centralized in `openai-client`.
