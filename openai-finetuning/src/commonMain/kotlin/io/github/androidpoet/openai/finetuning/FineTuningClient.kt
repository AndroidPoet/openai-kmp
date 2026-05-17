package io.github.androidpoet.openai.finetuning

import io.github.androidpoet.openai.client.OpenAIClient
import io.github.androidpoet.openai.client.defaultJson
import io.github.androidpoet.openai.client.toJsonBody
import io.github.androidpoet.openai.core.models.OpenAIListResponse
import io.github.androidpoet.openai.core.result.OpenAIResult
import io.github.androidpoet.openai.client.deserialize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class FineTuningJobCreateRequest(
    public val model: String,
    @SerialName("training_file")
    public val trainingFile: String,
    @SerialName("validation_file")
    public val validationFile: String? = null,
    @SerialName("hyperparameters")
    public val hyperParameters: JsonElement? = null,
    public val suffix: String? = null,
)

@Serializable
public data class FineTuningJobObject(
    public val id: String? = null,
    public val status: String? = null,
    public val model: String? = null,
    @SerialName("fine_tuned_model")
    public val fineTunedModel: String? = null,
    @SerialName("training_file")
    public val trainingFile: String? = null,
)

public interface FineTuningClient {
    public suspend fun createJob(body: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun retrieveJob(jobId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun listJobs(queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun cancelJob(jobId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun events(jobId: String, queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun pause(jobId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun resume(jobId: String, headers: Map<String, String> = emptyMap()): OpenAIResult<String>
    public suspend fun checkpoints(jobId: String, queryParams: List<Pair<String, String>> = emptyList(), headers: Map<String, String> = emptyMap()): OpenAIResult<String>
}

internal class FineTuningClientImpl(private val client: OpenAIClient) : FineTuningClient {
    override suspend fun createJob(body: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("fine_tuning/jobs", body, headers)

    override suspend fun retrieveJob(jobId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.get("fine_tuning/jobs/$jobId", headers = headers)

    override suspend fun listJobs(
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("fine_tuning/jobs", queryParams, headers)

    override suspend fun cancelJob(jobId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("fine_tuning/jobs/$jobId/cancel", headers = headers)

    override suspend fun events(
        jobId: String,
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("fine_tuning/jobs/$jobId/events", queryParams, headers)

    override suspend fun pause(jobId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("fine_tuning/jobs/$jobId/pause", headers = headers)

    override suspend fun resume(jobId: String, headers: Map<String, String>): OpenAIResult<String> =
        client.post("fine_tuning/jobs/$jobId/resume", headers = headers)

    override suspend fun checkpoints(
        jobId: String,
        queryParams: List<Pair<String, String>>,
        headers: Map<String, String>,
    ): OpenAIResult<String> = client.get("fine_tuning/jobs/$jobId/checkpoints", queryParams, headers)
}

public fun OpenAIClient.fineTuning(): FineTuningClient = FineTuningClientImpl(this)

public suspend fun FineTuningClient.createJobTyped(
    request: FineTuningJobCreateRequest,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<FineTuningJobObject> = createJob(request.toJsonBody(defaultJson), headers).deserialize()

public suspend fun FineTuningClient.retrieveJobTyped(
    jobId: String,
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<FineTuningJobObject> = retrieveJob(jobId, headers).deserialize()

public suspend fun FineTuningClient.listJobsTyped(
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<FineTuningJobObject>> = listJobs(queryParams, headers).deserialize()

public suspend fun FineTuningClient.eventsTyped(
    jobId: String,
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<JsonElement>> = events(jobId, queryParams, headers).deserialize()

public suspend fun FineTuningClient.checkpointsTyped(
    jobId: String,
    queryParams: List<Pair<String, String>> = emptyList(),
    headers: Map<String, String> = emptyMap(),
): OpenAIResult<OpenAIListResponse<JsonElement>> = checkpoints(jobId, queryParams, headers).deserialize()
