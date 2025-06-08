package org.tutvsisvoyi.testkmpapp.domain.model

data class Workspace(
    val id: Long,
    val name: String,
    val organizationId: Long? = null,
    val premium: Boolean = false
)