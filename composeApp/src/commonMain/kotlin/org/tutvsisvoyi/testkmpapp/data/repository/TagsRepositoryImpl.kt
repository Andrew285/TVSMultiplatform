package org.tutvsisvoyi.testkmpapp.data.repository

import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.domain.model.Tag
import org.tutvsisvoyi.testkmpapp.domain.repository.TagsRepository

class TagsRepositoryImpl(
    private val togglApiClient: TogglApiClient
): TagsRepository {
    override suspend fun getTagsByWorkspaceId(workspaceId: Int): Result<List<Tag>> {
        return togglApiClient.getTagsByWorkspace(workspaceId).fold(
            onSuccess = { tagsResponse ->
                val resultTags = tagsResponse.map { tag ->
                    Tag(
                        id = tag.id,
                        name = tag.name,
                        workspaceId = tag.workspaceId
                    )
                }
                Result.success(resultTags)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}