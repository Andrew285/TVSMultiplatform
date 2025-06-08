package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TogglTimeEntryResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("description")
    val description: String? = null,

    @SerialName("project_id")
    val projectId: Long? = null,

    @SerialName("workspace_id")
    val workspaceId: Long,

    @SerialName("start")
    val start: String,

    @SerialName("stop")
    val stop: String? = null,

    @SerialName("duration")
    val duration: Long,

    @SerialName("tags")
    val tags: List<String> = emptyList()
)