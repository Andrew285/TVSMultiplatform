package org.tutvsisvoyi.testkmpapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.data.network.model.CreateTimeEntryRequest
import org.tutvsisvoyi.testkmpapp.data.network.model.TogglTimeEntryResponse
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import kotlin.time.ExperimentalTime

class TimeEntryRepositoryImpl(
    private val togglApiClient: TogglApiClient,
) : TimeEntryRepository {


    @OptIn(ExperimentalTime::class)
    override suspend fun getTimeEntries(
        workspaceId: Long,
        startDate: String?,
        endDate: String?
    ): Flow<List<TimeEntry>> {

        val start = startDate?.let { LocalDate.parse(it) }
        val end = endDate?.let { LocalDate.parse(it) }

        val startMillis = start?.atStartOfDayIn(kotlinx.datetime.TimeZone.currentSystemDefault())?.toEpochMilliseconds()
        val endMillis = end?.atTime(23, 59, 59)?.toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())?.toEpochMilliseconds()

        return togglApiClient.getTimeEntries(startDate, endDate).fold(
            onSuccess = { entriesList ->
                val timeEntries = entriesList.map { entry ->
                    TimeEntry(
                        id = entry.id,
                        description = entry.description,
                        projectId = entry.projectId,
                        workspaceId = entry.workspaceId,
                        start = Instant.parse(entry.start),
                        stop = entry.stop?.let { Instant.parse(it) } ,
                        duration = entry.duration,
                        tags = entry.tags ?: emptyList(),
                    )
                }
                flowOf(timeEntries)
            },
            onFailure = {
                flowOf(emptyList())
            }
        )
//
//        val start = startDate?.let { kotlinx.datetime.LocalDate.parse(it) }
//        val end = endDate?.let { kotlinx.datetime.LocalDate.parse(it) }
//        return realmDatabase.getTimeEntries(workspaceId, start, end)
    }

    override suspend fun getCurrentTimeEntry(): TimeEntry? {
        return togglApiClient.getCurrentTimeEntry().getOrNull()?.let { response ->
            mapResponseToTimeEntry(response)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun startTimer(
        description: String,
        projectId: Long?,
        taskId: Long?,
        tags: List<String>,
        billable: Boolean?,
        workspaceId: Long
    ): Result<TimeEntry> {
        val request = CreateTimeEntryRequest(
            description = description,
            projectId = projectId,
            taskId = taskId,
            tags = tags,
            start = Clock.System.now().toString(), // Current time in ISO format
            workspaceId = workspaceId,
            billable = billable,
            duration = -1 // Indicates running timer
        )

        return togglApiClient.createNewTimeEntry(workspaceId, request).mapCatching { response ->
            response!!.toTimeEntry()
        }
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
//                realmDatabase.saveTimeEntries(timeEntries)
                Result.success(timeEntries)
            },
            onFailure = { Result.failure(it) }
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun mapResponseToTimeEntry(response: TogglTimeEntryResponse): TimeEntry {
        return TimeEntry(
            id = response.id,
            description = response.description,
            projectId = response.projectId,
            workspaceId = response.workspaceId,
            start = Instant.parse(response.start),
            stop = response.stop?.let { Instant.parse(it) },
            duration = response.duration,
            tags = response.tags ?: emptyList()
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun TogglTimeEntryResponse.toTimeEntry(): TimeEntry {
    return TimeEntry(
        id = this.id,
        workspaceId = this.workspaceId,
        projectId = this.projectId,
        description = this.description,
        start = Instant.parse(this.start),
        stop = this.stop?.let { Instant.parse(it) },
        duration = this.duration,
        tags = this.tags ?: emptyList(),
        project = null,
    )
}