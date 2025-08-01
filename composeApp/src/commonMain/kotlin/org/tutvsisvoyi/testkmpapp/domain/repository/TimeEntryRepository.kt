package org.tutvsisvoyi.testkmpapp.domain.repository

import kotlinx.coroutines.flow.Flow
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

interface TimeEntryRepository {
    suspend fun getTimeEntries(workspaceId: Long, startDate: String? = null, endDate: String? = null): Flow<List<TimeEntry>>
    suspend fun getCurrentTimeEntry(): TimeEntry?
    suspend fun startTimer(description: String, projectId: Long?, taskId: Long?, tags: List<String>, billable: Boolean?, workspaceId: Long): Result<TimeEntry>
    suspend fun stopTimer(): Result<TimeEntry?>
    suspend fun createTimeEntry(timeEntry: TimeEntry): Result<TimeEntry>
    suspend fun updateTimeEntry(timeEntry: TimeEntry): Result<TimeEntry>
    suspend fun deleteTimeEntry(workspaceId: Long, timeEntryId: Long): Result<Unit>
    suspend fun syncTimeEntries(workspaceId: Long): Result<List<TimeEntry>>
}