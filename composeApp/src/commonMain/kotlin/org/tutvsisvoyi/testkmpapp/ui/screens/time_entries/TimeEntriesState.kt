package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.Tag
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

data class TimeEntriesState(
    val timeEntries: List<TimeEntry> = emptyList(),
    val currentTimeEntry: TimeEntry? = null,
    val projects: List<Project> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val currentWorkspaceId: Long? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false
) {
    val isError: Boolean get() = errorMessage != null
    val hasWorkspace: Boolean get() = currentWorkspaceId != null
}