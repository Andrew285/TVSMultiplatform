package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TogglUserResponse(
    @SerialName("id")
    val id: Long,

    @SerialName("email")
    val email: String,

    @SerialName("fullname")
    val fullName: String,

    @SerialName("default_workspace_id")
    val defaultWorkspaceId: Long,

    @SerialName("timezone")
    val timezone: String,

    @SerialName("image_url")
    val imageUrl: String? = null
)