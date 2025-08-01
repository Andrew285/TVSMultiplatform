package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.tutvsisvoyi.testkmpapp.data.utils.CalendarUtils
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TagsRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

class TimeEntriesScreenModel(
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val projectsRepository: ProjectRepository,
    private val tagsRepository: TagsRepository
) : ScreenModel {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    private var currentStartDate: String? = CalendarUtils.firstDayOfThisMonth().toString()
    private var currentEndDate: String? = CalendarUtils.today().toString()
    private var timeEntriesJob: Job? = null

    init {
        initializeScreen()
    }

    private fun initializeScreen() {
        screenModelScope.launch {
            // First get the current workspace
            val currentWorkspace = workspaceRepository.getCurrentWorkspace()
            if (currentWorkspace != null) {
                _state.value = _state.value.copy(currentWorkspaceId = currentWorkspace.id)
                startTimeEntriesCollection() // Start collecting the Flow
                loadProjects()
                loadTagsByWorkspace()
            } else {
                // Try to sync workspaces first
                workspaceRepository.syncWorkspaces().fold(
                    onSuccess = { workspaces ->
                        if (workspaces.isNotEmpty()) {
                            val firstWorkspace = workspaces.first()
                            workspaceRepository.setCurrentWorkspace(firstWorkspace)
                            _state.value = _state.value.copy(currentWorkspaceId = firstWorkspace.id)
                            startTimeEntriesCollection()
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
        }
    }

    // FIXED: Single Flow collection that automatically updates UI
    private fun startTimeEntriesCollection() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        // Cancel previous collection
        timeEntriesJob?.cancel()

        timeEntriesJob = screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                timeEntryRepository.getTimeEntries(
                    workspaceId = workspaceId,
                    startDate = currentStartDate,
                    endDate = currentEndDate
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

    // FIXED: Simple refresh that just syncs - the Flow will auto-update UI
    private fun refreshTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            timeEntryRepository.syncTimeEntries(workspaceId).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isRefreshing = false)
                    // The Flow collection will automatically update the UI with new data
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

    // REMOVED: loadTimeEntries() - replaced with startTimeEntriesCollection()

    private fun selectDateRange(startDate: String?, endDate: String?) {
        currentStartDate = startDate
        currentEndDate = endDate
        // Restart collection with new date range
        startTimeEntriesCollection()
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
                currentState.copy(
                    isSyncing = true,
                    errorMessage = null
                )
            }

            timeEntryRepository.startTimer(
                description, projectId, taskId, tags, billable, workspaceId
            )
                .onSuccess { entry ->
                    _state.update { currentState ->
                        currentState.copy(
                            isSyncing = false,
                            currentTimeEntry = entry
                        )
                    }
                    // The Flow will automatically update with the new entry from the database
                }
                .onFailure { error ->
                    _state.update { currentState ->
                        currentState.copy(
                            isSyncing = false,
                            currentTimeEntry = null,
                            errorMessage = error.message
                        )
                    }
                    println("Failed to start timer: ${error.message}")
                }
        }
    }

    fun handleAction(action: TimeEntriesAction) {
        when (action) {
            is TimeEntriesAction.LoadTimeEntries -> startTimeEntriesCollection()
            is TimeEntriesAction.RefreshTimeEntries -> refreshTimeEntries()
            is TimeEntriesAction.SyncTimeEntries -> syncTimeEntries()
            is TimeEntriesAction.ClearError -> clearError()
            is TimeEntriesAction.SelectDateRange -> selectDateRange(action.startDate, action.endDate)
            is TimeEntriesAction.LoadProjects -> loadProjects()
            is TimeEntriesAction.CreateTimeEntry -> startTimer(
                description = action.description,
                projectId = action.projectId,
                taskId = action.taskId,
                tags = action.tags,
                billable = action.billable,
                workspaceId = action.workspaceId,
            )
        }
    }

    // Rest of your methods remain the same...
    private fun loadProjects() {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                projectsRepository.getUserProjects().fold(
                    onSuccess = { response ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = null,
                            projects = response
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "Loading projects failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "Loading projects failed: ${e.message}"
                )
            }
        }
    }

    private fun loadTagsByWorkspace() {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                isRefreshing = false,
                errorMessage = null
            )

            val workspaceId = _state.value.currentWorkspaceId ?: return@launch
            tagsRepository.getTagsByWorkspaceId(workspaceId.toInt()).fold(
                onSuccess = { response ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        tags = response
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load tags: ${error.message}"
                    )
                }
            )
        }
    }

    private fun syncTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(
                isSyncing = true,
                errorMessage = null
            )

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

    override fun onDispose() {
        super.onDispose()
        timeEntriesJob?.cancel()
    }
}