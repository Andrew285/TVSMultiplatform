package org.tutvsisvoyi.testkmpapp.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.tutvsisvoyi.testkmpapp.data.database.realm.RealmDatabase
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

class WorkspaceRepositoryImpl(
    private val togglApiClient: TogglApiClient,
    private val realmDatabase: RealmDatabase,
    private val settings: Settings
) : WorkspaceRepository {

    override suspend fun getWorkspaces(): Flow<List<Workspace>> {
        return realmDatabase.getWorkspaces()
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
                realmDatabase.saveWorkspaces(workspaces)
                Result.success(workspaces)
            },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun getCurrentWorkspace(): Workspace? {
        val workspaceId = settings.getLongOrNull("current_workspace_id")
        return if (workspaceId != null) {
            realmDatabase.getWorkspaces().first().find { it.id == workspaceId }
        } else {
            null
        }
    }

    override suspend fun setCurrentWorkspace(workspace: Workspace) {
        settings.putLong("current_workspace_id", workspace.id)
    }
}