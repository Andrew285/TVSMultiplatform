package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

data class TimeEntriesState(
    val timeEntries: List<TimeEntry> = emptyList(),
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