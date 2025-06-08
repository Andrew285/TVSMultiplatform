package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionResponse(
    val data: TogglUserResponse
)