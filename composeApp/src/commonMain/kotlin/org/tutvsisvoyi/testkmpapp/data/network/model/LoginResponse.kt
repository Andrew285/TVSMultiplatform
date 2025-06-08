package org.tutvsisvoyi.testkmpapp.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val data: TogglUserResponse,
    @SerialName("api_token") val apiToken: String? = null
)