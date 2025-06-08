package org.tutvsisvoyi.testkmpapp.domain.model

data class User(
    val id: Long,
    val email: String,
    val fullName: String,
    val defaultWorkspaceId: Long,
    val timezone: String,
    val imageUrl: String? = null
)