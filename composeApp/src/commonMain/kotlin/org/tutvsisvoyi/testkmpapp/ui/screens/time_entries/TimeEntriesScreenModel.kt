package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import androidx.compose.material3.ExperimentalMaterial3Api
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.tutvsisvoyi.testkmpapp.data.utils.CalendarUtils
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TagsRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

@OptIn(ExperimentalMaterial3Api::class)
class TimeEntriesScreenModel(
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val projectsRepository: ProjectRepository,
    private val tagsRepository: TagsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    private var timeEntriesJob: Job? = null

    init {
        initializeScreen()
    }

    private fun initializeScreen() {
        screenModelScope.launch {
            loadWorkspaces()

            val currentWorkspace = workspaceRepository.getCurrentWorkspace()
            if (currentWorkspace != null) {
                _state.value = _state.value.copy(currentWorkspaceId = currentWorkspace.id)
                applyDateFilter(DateFilterType.THIS_MONTH)
                loadProjects()
                loadTagsByWorkspace()
            } else {
                syncAndSelectFirstWorkspace()
            }
        }
    }

    private suspend fun syncAndSelectFirstWorkspace() {
        workspaceRepository.syncWorkspaces().fold(
            onSuccess = { workspaces ->
                if (workspaces.isNotEmpty()) {
                    val firstWorkspace = workspaces.first()
                    workspaceRepository.setCurrentWorkspace(firstWorkspace)
                    _state.value = _state.value.copy(
                        workspaces = workspaces,
                        currentWorkspaceId = firstWorkspace.id
                    )
                    applyDateFilter(DateFilterType.THIS_MONTH)
                    loadProjects()
                    loadTagsByWorkspace()
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "No workspaces found. Please check your account."
                    )
                }
            },
            onFailure = { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load workspaces: ${error.message}"
                )
            }
        )
    }

    private fun loadWorkspaces() {
        screenModelScope.launch {
            workspaceRepository.getWorkspaces().collect { workspaces ->
                _state.value = _state.value.copy(workspaces = workspaces)
            }
        }
    }

    private fun selectWorkspace(workspace: org.tutvsisvoyi.testkmpapp.domain.model.Workspace) {
        screenModelScope.launch {
            workspaceRepository.setCurrentWorkspace(workspace)
            _state.value = _state.value.copy(
                currentWorkspaceId = workspace.id,
                showWorkspaceSelector = false
            )

            // Reload data for new workspace
            applyDateFilter(_state.value.dateFilterType)
            loadProjects()
            loadTagsByWorkspace()
        }
    }

    private fun applyDateFilter(filterType: DateFilterType) {
        val today = CalendarUtils.today()
        val (startDate, endDate) = when (filterType) {
            DateFilterType.TODAY -> {
                today.toString() to today.toString()
            }
            DateFilterType.YESTERDAY -> {
                val yesterday = today.minus(1, DateTimeUnit.DAY)
                yesterday.toString() to yesterday.toString()
            }
            DateFilterType.THIS_WEEK -> {
                val startOfWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                startOfWeek.toString() to today.toString()
            }
            DateFilterType.LAST_WEEK -> {
                val startOfLastWeek = today.minus(today.dayOfWeek.ordinal + 7, DateTimeUnit.DAY)
                val endOfLastWeek = startOfLastWeek.plus(6, DateTimeUnit.DAY)
                startOfLastWeek.toString() to endOfLastWeek.toString()
            }
            DateFilterType.THIS_MONTH -> {
                CalendarUtils.firstDayOfThisMonth().toString() to today.toString()
            }
            DateFilterType.LAST_MONTH -> {
                val firstDayLastMonth = today.minus(1, DateTimeUnit.MONTH)
                    .let { LocalDate(it.year, it.month, 1) }
                val lastDayLastMonth = firstDayLastMonth.plus(1, DateTimeUnit.MONTH)
                    .minus(1, DateTimeUnit.DAY)
                firstDayLastMonth.toString() to lastDayLastMonth.toString()
            }
            DateFilterType.CUSTOM -> {
                // Keep current custom dates
                _state.value.selectedStartDate to _state.value.selectedEndDate
            }
        }

        _state.value = _state.value.copy(
            dateFilterType = filterType,
            selectedStartDate = startDate,
            selectedEndDate = endDate
        )

        startTimeEntriesCollection()
    }

    private fun selectCustomDateRange(startDate: String?, endDate: String?) {
        _state.value = _state.value.copy(
            dateFilterType = DateFilterType.CUSTOM,
            selectedStartDate = startDate,
            selectedEndDate = endDate
        )
        startTimeEntriesCollection()
    }

    private fun startTimeEntriesCollection() {
        val workspaceId = _state.value.currentWorkspaceId ?: return
        val startDate = _state.value.selectedStartDate
        val endDate = _state.value.selectedEndDate

        timeEntriesJob?.cancel()
        timeEntriesJob = screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                timeEntryRepository.getTimeEntries(
                    workspaceId = workspaceId,
                    startDate = startDate,
                    endDate = endDate
                ).collect { entries ->
                    _state.value = _state.value.copy(
                        timeEntries = entries,
                        isLoading = false,
                        isEmpty = entries.isEmpty(),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load time entries: ${e.message}"
                )
            }
        }
    }

    private fun refreshTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, errorMessage = null)

            timeEntryRepository.syncTimeEntries(workspaceId).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isRefreshing = false)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        errorMessage = "Sync failed: ${error.message}"
                    )
                }
            )
        }
    }

    private fun swipeToDelete(timeEntry: org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry) {
        _state.value = _state.value.copy(
            showDeleteConfirmation = true,
            entryToDelete = timeEntry
        )
    }

    private fun deleteTimeEntry(timeEntry: org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry) {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(
                showDeleteConfirmation = false,
                entryToDelete = null
            )

            timeEntryRepository.deleteTimeEntry(workspaceId, timeEntry.id!!).fold(
                onSuccess = {
                    // The Flow will automatically update the UI
                    println("Time entry deleted successfully")
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        errorMessage = "Failed to delete time entry: ${error.message}"
                    )
                }
            )
        }
    }

    private fun startTimer(
        description: String,
        projectId: Long? = null,
        taskId: Long? = null,
        tags: List<String> = emptyList(),
        billable: Boolean? = null,
        workspaceId: Long
    ) {
        screenModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isSyncing = true, errorMessage = null)
            }

            timeEntryRepository.startTimer(
                description, projectId, taskId, tags, billable, workspaceId
            ).onSuccess { entry ->
                _state.update { currentState ->
                    currentState.copy(isSyncing = false, currentTimeEntry = entry)
                }
            }.onFailure { error ->
                _state.update { currentState ->
                    currentState.copy(
                        isSyncing = false,
                        currentTimeEntry = null,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    private fun loadProjects() {
        screenModelScope.launch {
            projectsRepository.getUserProjects().fold(
                onSuccess = { projects ->
                    _state.value = _state.value.copy(projects = projects)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        errorMessage = "Loading projects failed: ${error.message}"
                    )
                }
            )
        }
    }

    private fun loadTagsByWorkspace() {
        screenModelScope.launch {
            val workspaceId = _state.value.currentWorkspaceId ?: return@launch
            tagsRepository.getTagsByWorkspaceId(workspaceId.toInt()).fold(
                onSuccess = { tags ->
                    _state.value = _state.value.copy(tags = tags)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        errorMessage = "Failed to load tags: ${error.message}"
                    )
                }
            )
        }
    }

    private fun syncTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, errorMessage = null)

            timeEntryRepository.syncTimeEntries(workspaceId).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isSyncing = false)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isSyncing = false,
                        errorMessage = "Sync failed: ${error.message}"
                    )
                }
            )
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun handleAction(action: TimeEntriesAction) {
        when (action) {
            is TimeEntriesAction.LoadTimeEntries -> startTimeEntriesCollection()
            is TimeEntriesAction.RefreshTimeEntries -> refreshTimeEntries()
            is TimeEntriesAction.SyncTimeEntries -> syncTimeEntries()
            is TimeEntriesAction.ClearError -> clearError()
            is TimeEntriesAction.LoadProjects -> loadProjects()
            is TimeEntriesAction.LoadWorkspaces -> loadWorkspaces()
            is TimeEntriesAction.ShowWorkspaceSelector -> {
                _state.value = _state.value.copy(showWorkspaceSelector = true)
            }
            is TimeEntriesAction.HideWorkspaceSelector -> {
                _state.value = _state.value.copy(showWorkspaceSelector = false)
            }
            is TimeEntriesAction.SelectWorkspace -> selectWorkspace(action.workspace)
            is TimeEntriesAction.SelectDateRange -> selectCustomDateRange(action.startDate, action.endDate)
            is TimeEntriesAction.SelectDateFilter -> applyDateFilter(action.filterType)
            is TimeEntriesAction.SwipeToDelete -> swipeToDelete(action.timeEntry)
            is TimeEntriesAction.DeleteTimeEntry -> deleteTimeEntry(action.timeEntry)
            is TimeEntriesAction.ShowDeleteConfirmation -> {
                _state.value = _state.value.copy(showDeleteConfirmation = true)
            }
            is TimeEntriesAction.HideDeleteConfirmation -> {
                _state.value = _state.value.copy(
                    showDeleteConfirmation = false,
                    entryToDelete = null
                )
            }
            is TimeEntriesAction.CreateTimeEntry -> startTimer(
                description = action.description,
                projectId = action.projectId,
                taskId = action.taskId,
                tags = action.tags,
                billable = action.billable,
                workspaceId = action.workspaceId
            )
        }
    }

    override fun onDispose() {
        super.onDispose()
        timeEntriesJob?.cancel()
    }
}