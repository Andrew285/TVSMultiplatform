package org.tutvsisvoyi.testkmpapp.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

class WorkspaceRepositoryImpl(
    private val togglApiClient: TogglApiClient,
    private val settings: Settings
) : WorkspaceRepository {

    override suspend fun getWorkspaces(): Flow<List<Workspace>> {
        return try {
            togglApiClient.getWorkspaces().fold(
                onSuccess = { workspaces ->
                    val mappedWorkspaces = workspaces.map { workspace ->
                        Workspace(
                            id = workspace.id,
                            name = workspace.name,
                            organizationId = workspace.organizationId,
                            premium = workspace.premium
                        )
                    }
                    flowOf(mappedWorkspaces)
                },
                onFailure = {
                    flowOf(emptyList())
                }
            )
        } catch (e: Exception) {
            flowOf(emptyList())
        }
    }

    override suspend fun syncWorkspaces(): Result<List<Workspace>> {
        return togglApiClient.getWorkspaces().fold(
            onSuccess = { responses ->
                val workspaces = responses.map { response ->
                    Workspace(
                        id = response.id,
                        name = response.name,
                        organizationId = response.organizationId,
                        premium = response.premium
                    )
                }
//                realmDatabase.saveWorkspaces(workspaces)
                Result.success(workspaces)
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun getCurrentWorkspace(): Workspace? {
        val workspaceId = settings.getLongOrNull("current_workspace_id")
        return if (workspaceId != null) {
            getWorkspaces().first().find { it.id == workspaceId }
//            realmDatabase.getWorkspaces().first().find { it.id == workspaceId }
        } else {
            null
        }
    }

    override suspend fun setCurrentWorkspace(workspace: Workspace) {
        settings.putLong("current_workspace_id", workspace.id)
    }
}