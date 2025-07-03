package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository

class TimeEntriesScreenModel(
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val projectsRepository: ProjectRepository
) : ScreenModel {

    private val _state = MutableStateFlow(TimeEntriesState())
    val state: StateFlow<TimeEntriesState> = _state.asStateFlow()

    private var currentStartDate: String? = null
    private var currentEndDate: String? = null

    init {
        initializeScreen()
    }

    fun handleAction(action: TimeEntriesAction) {
        when (action) {
            is TimeEntriesAction.LoadTimeEntries -> loadTimeEntries()
            is TimeEntriesAction.RefreshTimeEntries -> refreshTimeEntries()
            is TimeEntriesAction.SyncTimeEntries -> syncTimeEntries()
            is TimeEntriesAction.ClearError -> clearError()
            is TimeEntriesAction.SelectDateRange -> selectDateRange(action.startDate, action.endDate)
            is TimeEntriesAction.LoadProjects -> loadProjects()
        }
    }

    private fun initializeScreen() {
        screenModelScope.launch {
            // First get the current workspace
            val currentWorkspace = workspaceRepository.getCurrentWorkspace()
            if (currentWorkspace != null) {
                _state.value = _state.value.copy(currentWorkspaceId = currentWorkspace.id)
                loadTimeEntries()
                loadProjects()
            } else {
                // Try to sync workspaces first
                workspaceRepository.syncWorkspaces().fold(
                    onSuccess = { workspaces ->
                        if (workspaces.isNotEmpty()) {
                            // Set first workspace as current
                            val firstWorkspace = workspaces.first()
                            workspaceRepository.setCurrentWorkspace(firstWorkspace)
                            _state.value = _state.value.copy(currentWorkspaceId = firstWorkspace.id)
                            loadTimeEntries()
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

    private fun loadTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null
            )

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

    private fun refreshTimeEntries() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        screenModelScope.launch {
            _state.value = _state.value.copy(
                isRefreshing = true,
                errorMessage = null
            )

            try {
                // Sync with remote first
                timeEntryRepository.syncTimeEntries(workspaceId).fold(
                    onSuccess = {
                        // Data will be updated through the Flow
                        _state.value = _state.value.copy(isRefreshing = false)
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isRefreshing = false,
                            errorMessage = "Sync failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    errorMessage = "Refresh failed: ${e.message}"
                )
            }
        }
    }

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
                    errorMessage = "Loading projects failed: ${e.message}"
                )
            }
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

    private fun selectDateRange(startDate: String?, endDate: String?) {
        currentStartDate = startDate
        currentEndDate = endDate
        loadTimeEntries()
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

//    fun onPullToRefreshTrigger() {
//        _state.value = _state.value.copy(
//            isRefreshing = true,
//            errorMessage = null
//        )
//        viewModelScope.launch {
//            animalFactsRepository.fetchAnimalFacts()
//            _state.value = _state.value.copy(
//                isRefreshing = false,
//                errorMessage = null
//            )
//        }
//    }
}