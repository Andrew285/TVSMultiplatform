package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTimeEntryRequest(
    @SerialName("billable")
    val billable: Boolean? = null,

    @SerialName("created_with")
    val createdWith: String = "TVS Multiplatform",

    @SerialName("description")
    val description: String? = null,

    @SerialName("project_id")
    val projectId: Long? = null,

    @SerialName("task_id")
    val taskId: Long? = null,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("start")
    val start: String, // ISO 8601 format: "2024-01-15T10:30:00.000Z"

    @SerialName("stop")
    val stop: String? = null, // null for running entries

    @SerialName("duration")
    val duration: Long = -1, // -1 for running entries, positive for completed

    @SerialName("workspace_id")
    val workspaceId: Long
)