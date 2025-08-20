package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.model.Workspace

sealed class TimeEntriesAction {
    object LoadTimeEntries : TimeEntriesAction()
    object RefreshTimeEntries : TimeEntriesAction()
    object SyncTimeEntries : TimeEntriesAction()
    object ClearError : TimeEntriesAction()
    object LoadProjects : TimeEntriesAction()
    object LoadWorkspaces : TimeEntriesAction()
    object ShowWorkspaceSelector : TimeEntriesAction()
    object HideWorkspaceSelector : TimeEntriesAction()
    object ShowDeleteConfirmation : TimeEntriesAction()
    object HideDeleteConfirmation : TimeEntriesAction()

    data class SelectWorkspace(val workspace: Workspace) : TimeEntriesAction()
    data class CreateTimeEntry(
        val description: String,
        val projectId: Long?,
        val taskId: Long?,
        val tags: List<String>,
        val billable: Boolean,
        val workspaceId: Long
    ) : TimeEntriesAction()
    data class SelectDateRange(val startDate: String?, val endDate: String?) : TimeEntriesAction()
    data class SelectDateFilter(val filterType: DateFilterType) : TimeEntriesAction()
    data class SwipeToDelete(val timeEntry: TimeEntry) : TimeEntriesAction()
    data class DeleteTimeEntry(val timeEntry: TimeEntry) : TimeEntriesAction()
}
