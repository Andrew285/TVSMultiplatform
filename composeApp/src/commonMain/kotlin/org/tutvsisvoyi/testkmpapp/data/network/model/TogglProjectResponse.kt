package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TogglProjectResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("name")
    val name: String,

    @SerialName("workspace_id")
    val workspaceId: Long,

    @SerialName("color")
    val color: String? = null,

    @SerialName("active")
    val active: Boolean = true,

    @SerialName("client_id")
    val clientId: Long? = null
)