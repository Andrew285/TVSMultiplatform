package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.Tag
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace

data class TimeEntriesState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val timeEntries: List<TimeEntry> = emptyList(),
    val selectedDateRange: DateRangePickerState? = null,
    val currentTimeEntry: TimeEntry? = null,
    val projects: List<Project> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val workspaces: List<Workspace> = emptyList(), // Add workspaces list
    val currentWorkspaceId: Long? = null,
    val selectedStartDate: String? = null, // Better date handling
    val selectedEndDate: String? = null,
    val dateFilterType: DateFilterType = DateFilterType.THIS_MONTH,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val showWorkspaceSelector: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val entryToDelete: TimeEntry? = null
) {
    val isError: Boolean get() = errorMessage != null
    val hasWorkspace: Boolean get() = currentWorkspaceId != null
    val currentWorkspace: Workspace? get() = workspaces.find { it.id == currentWorkspaceId }
}

enum class DateFilterType {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}