package org.tutvsisvoyi.testkmpapp.data.repository

import kotlinx.coroutines.flow.Flow
import org.tutvsisvoyi.testkmpapp.data.database.realm.RealmDatabase
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTimeEntryResponse
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository

class TimeEntryRepositoryImpl(
    private val togglApiClient: TogglApiClient,
    private val realmDatabase: RealmDatabase
) : TimeEntryRepository {

    override suspend fun getTimeEntries(
        workspaceId: Long,
        startDate: String?,
        endDate: String?
    ): Flow<List<TimeEntry>> {
        val start = startDate?.let { kotlinx.datetime.LocalDate.parse(it) }
        val end = endDate?.let { kotlinx.datetime.LocalDate.parse(it) }
        return realmDatabase.getTimeEntries(workspaceId, start, end)
    }

    override suspend fun getCurrentTimeEntry(): TimeEntry? {
        return togglApiClient.getCurrentTimeEntry().getOrNull()?.let { response ->
            mapResponseToTimeEntry(response)
        }
    }

    override suspend fun startTimer(
        workspaceId: Long,
        description: String,
        projectId: Long?
    ): Result<TimeEntry> {
//        val request = CreateTimeEntryRequest(
//            description = description,
//            projectId = projectId,
//            workspaceId = workspaceId,
//            start = kotlinx.datetime.Clock.System.now().toString(),
//            duration = -1
//        )
//
//        return togglApiClient.createTimeEntry(workspaceId, request).map { response ->
//            mapResponseToTimeEntry(response)
//        }
        TODO("Implement create time entry")

    }

    override suspend fun stopTimer(): Result<TimeEntry?> {
//        return togglApiClient.getCurrentTimeEntry().fold(
//            onSuccess = { currentEntry ->
//                if (currentEntry != null) {
//                    togglApiClient.stopTimeEntry(currentEntry.workspaceId, currentEntry.id)
//                        .map { response -> mapResponseToTimeEntry(response) }
//                } else {
//                    Result.success(null)
//                }
//            },
//            onFailure = { Result.failure(it) }
//        )
        TODO("Implement create time entry")

    }

    override suspend fun createTimeEntry(timeEntry: TimeEntry): Result<TimeEntry> {
        TODO("Implement create time entry")
    }

    override suspend fun updateTimeEntry(timeEntry: TimeEntry): Result<TimeEntry> {
        TODO("Implement update time entry")
    }

    override suspend fun deleteTimeEntry(workspaceId: Long, timeEntryId: Long): Result<Unit> {
        TODO("Implement delete time entry")
    }

    override suspend fun syncTimeEntries(workspaceId: Long): Result<List<TimeEntry>> {
        return togglApiClient.getTimeEntries().fold(
            onSuccess = { responses ->
                val timeEntries = responses.map { response ->
                    mapResponseToTimeEntry(response)
                }
                realmDatabase.saveTimeEntries(timeEntries)
                Result.success(timeEntries)
            },
            onFailure = { Result.failure(it) }
        )
    }

    private fun mapResponseToTimeEntry(response: TogglTimeEntryResponse): TimeEntry {
        return TimeEntry(
            id = response.id,
            description = response.description,
            projectId = response.projectId,
            workspaceId = response.workspaceId,
            start = kotlinx.datetime.Instant.parse(response.start),
            stop = response.stop?.let { kotlinx.datetime.Instant.parse(it) },
            duration = response.duration,
            tags = response.tags
        )
    }
}