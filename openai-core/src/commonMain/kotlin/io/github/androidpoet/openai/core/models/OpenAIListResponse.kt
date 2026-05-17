package io.github.androidpoet.openai.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OpenAIListResponse<T>(
    public val `object`: String? = null,
    public val data: List<T> = emptyList(),
    @SerialName("has_more")
    public val hasMore: Boolean? = null,
    @SerialName("first_id")
    public val firstId: String? = null,
    @SerialName("last_id")
    public val lastId: String? = null,
)
