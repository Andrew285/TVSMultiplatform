package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

sealed class TimeEntriesAction {
    object LoadTimeEntries : TimeEntriesAction()
    object RefreshTimeEntries : TimeEntriesAction()
    object SyncTimeEntries : TimeEntriesAction()
    object ClearError : TimeEntriesAction()
    data class SelectDateRange(val startDate: String?, val endDate: String?) : TimeEntriesAction()
}
