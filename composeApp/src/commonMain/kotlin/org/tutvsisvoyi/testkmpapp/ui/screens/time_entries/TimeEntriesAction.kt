package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

sealed class TimeEntriesAction {
    object LoadTimeEntries : TimeEntriesAction()
    object RefreshTimeEntries : TimeEntriesAction()
    object SyncTimeEntries : TimeEntriesAction()
    object ClearError : TimeEntriesAction()
    object LoadProjects : TimeEntriesAction()
    data class CreateTimeEntry(
        val description: String,
        val projectId: Long?,
        val taskId: Long?,
        val tags: List<String>,
        val billable: Boolean,
        val workspaceId: Long
    ) : TimeEntriesAction()
    data class SelectDateRange(val startDate: String?, val endDate: String?) : TimeEntriesAction()
}
