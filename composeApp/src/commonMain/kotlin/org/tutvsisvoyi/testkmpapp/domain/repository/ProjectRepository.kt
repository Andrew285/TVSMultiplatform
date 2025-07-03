package org.tutvsisvoyi.testkmpapp.domain.repository

import org.tutvsisvoyi.testkmpapp.domain.model.Project

interface ProjectRepository {
    suspend fun getProjectsByWorkspace(): Result<List<Project>>
    suspend fun getUserProjects(): Result<List<Project>>
}