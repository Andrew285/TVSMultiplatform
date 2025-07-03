package org.tutvsisvoyi.testkmpapp.data.repository

import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository

class ProjectRepositoryImpl(
    private val togglApiClient: TogglApiClient,
): ProjectRepository {
    override suspend fun getProjectsByWorkspace(): Result<List<Project>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProjects(): Result<List<Project>> {
        return togglApiClient.getUserProjects().fold(
            onSuccess = { response ->
                val projects = response.map { responseProject ->
                    Project(
                        id = responseProject.id,
                        name = responseProject.name,
                        workspaceId = responseProject.workspaceId,
                        color = responseProject.color,
                        active = responseProject.active,
                        clientId = responseProject.clientId
                    )
                }
                Result.success(projects)
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

}