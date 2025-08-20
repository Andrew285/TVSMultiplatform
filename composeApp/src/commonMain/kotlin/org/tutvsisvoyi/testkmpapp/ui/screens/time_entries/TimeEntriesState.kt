package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.Tag
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

data class TimeEntriesState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val timeEntries: List<TimeEntry> = emptyList(),
    val selectedDateRange: DateRangePickerState? = null,
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