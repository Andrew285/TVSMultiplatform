package org.tutvsisvoyi.testkmpapp.domain.model

data class AuthResponse(
    val user: User,
    val apiToken: String
)