package org.tutvsisvoyi.testkmpapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.todayIn
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GetTodayTimeEntriesUseCase(
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(): Flow<List<TimeEntry>> {
        val workspace = workspaceRepository.getCurrentWorkspace()
            ?: throw Exception("No workspace selected")

        val today = Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).toString()
        return timeEntryRepository.getTimeEntries(workspace.id, today, today)
    }
}