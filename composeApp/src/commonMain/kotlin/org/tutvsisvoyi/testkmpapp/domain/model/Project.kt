package org.tutvsisvoyi.testkmpapp.domain.model

data class Project(
    val id: Long,
    val name: String,
    val workspaceId: Long,
    val color: String? = null,
    val active: Boolean = true,
    val clientId: Long? = null
)