package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TogglWorkspaceResponse(
    val id: Long,
    val name: String,
    @SerialName("organization_id")
    val organizationId: Long? = null,
    val premium: Boolean = false
)