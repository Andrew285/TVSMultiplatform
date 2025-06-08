package org.tutvsisvoyi.testkmpapp.domain.repository

import kotlinx.coroutines.flow.Flow
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace

interface WorkspaceRepository {
    suspend fun getWorkspaces(): Flow<List<Workspace>>
    suspend fun syncWorkspaces(): Result<List<Workspace>>
    suspend fun getCurrentWorkspace(): Workspace?
    suspend fun setCurrentWorkspace(workspace: Workspace)
}