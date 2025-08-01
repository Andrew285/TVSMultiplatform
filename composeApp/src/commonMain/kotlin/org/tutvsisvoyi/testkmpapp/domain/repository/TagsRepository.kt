package org.tutvsisvoyi.testkmpapp.domain.repository

import org.tutvsisvoyi.testkmpapp.domain.model.Tag

interface TagsRepository {
    suspend fun getTagsByWorkspaceId(workspaceId: Int): Result<List<Tag>>
}