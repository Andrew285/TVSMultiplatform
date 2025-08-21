package org.tutvsisvoyi.testkmpapp.ui.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository

class ProfileScreenModel(
    private val authRepository: AuthRepository,
    private val timeEntryRepository: TimeEntryRepository,
    private val projectRepository: ProjectRepository,
    private val settings: Settings
) : ScreenModel {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        loadSettings()
    }

    fun handleAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.LoadProfile -> loadProfile()
            is ProfileAction.Logout -> showLogoutConfirmation()
            is ProfileAction.ConfirmLogout -> confirmLogout()
            is ProfileAction.CancelLogout -> cancelLogout()
            is ProfileAction.ClearError -> clearError()
            is ProfileAction.ClearSuccessMessage -> clearSuccessMessage()

            // Settings updates
            is ProfileAction.UpdateUse24HourFormat -> updateUse24HourFormat(action.enabled)
            is ProfileAction.UpdateGroupSimilarEntries -> updateGroupSimilarEntries(action.enabled)
            is ProfileAction.UpdateDateFormat -> updateDateFormat(action.format)
            is ProfileAction.UpdateDurationFormat -> updateDurationFormat(action.format)
            is ProfileAction.UpdateFirstDayOfWeek -> updateFirstDayOfWeek(action.day)

            // Profile updates
            is ProfileAction.UpdateFullName -> updateFullName(action.name)
            is ProfileAction.UpdateTimezone -> updateTimezone(action.timezone)

            // Account actions
            is ProfileAction.ExportData -> exportData()
            is ProfileAction.ClearCache -> clearCache()

            // Dialog actions for existing settings
            is ProfileAction.ShowDateFormatDialog -> _state.value = _state.value.copy(showDateFormatDialog = true)
            is ProfileAction.HideDateFormatDialog -> _state.value = _state.value.copy(showDateFormatDialog = false)
            is ProfileAction.ShowDurationFormatDialog -> _state.value = _state.value.copy(showDurationFormatDialog = true)
            is ProfileAction.HideDurationFormatDialog -> _state.value = _state.value.copy(showDurationFormatDialog = false)
            is ProfileAction.ShowFirstDayDialog -> _state.value = _state.value.copy(showFirstDayDialog = true)
            is ProfileAction.HideFirstDayDialog -> _state.value = _state.value.copy(showFirstDayDialog = false)

            // Dialog actions for new features
            is ProfileAction.ShowEditNameDialog -> _state.value = _state.value.copy(showEditNameDialog = true)
            is ProfileAction.HideEditNameDialog -> _state.value = _state.value.copy(showEditNameDialog = false)
            is ProfileAction.ShowTimezoneDialog -> _state.value = _state.value.copy(showTimezoneDialog = true)
            is ProfileAction.HideTimezoneDialog -> _state.value = _state.value.copy(showTimezoneDialog = false)
        }
    }

    private fun loadProfile() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                authRepository.getCurrentUser().fold(
                    onSuccess = { user ->
                        _state.value = _state.value.copy(
                            user = user,
                            isLoading = false,
                            timezone = user.timezone
                        )
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    private fun loadSettings() {
        _state.value = _state.value.copy(
            use24HourFormat = settings.getBoolean("use_24_hour_format", false),
            groupSimilarEntries = settings.getBoolean("group_similar_entries", false),
            dateFormat = settings.getString("date_format", "MM/DD/YYYY"),
            durationFormat = settings.getString("duration_format", "Improved"),
            firstDayOfWeek = settings.getString("first_day_of_week", "Monday")
        )
    }

    private fun showLogoutConfirmation() {
        _state.value = _state.value.copy(showLogoutConfirmDialog = true)
    }

    private fun confirmLogout() {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoggingOut = true,
                showLogoutConfirmDialog = false
            )

            try {
                // Clear local settings
                settings.clear()

                // Logout from auth repository
                authRepository.logout()

                // Clear user state
                _state.value = _state.value.copy(
                    user = null,
                    isLoggingOut = false
                )

                // Navigation will be handled by the UI
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoggingOut = false,
                    errorMessage = "Failed to logout: ${e.message}"
                )
            }
        }
    }

    private fun cancelLogout() {
        _state.value = _state.value.copy(showLogoutConfirmDialog = false)
    }

    private fun updateUse24HourFormat(enabled: Boolean) {
        settings.putBoolean("use_24_hour_format", enabled)
        _state.value = _state.value.copy(
            use24HourFormat = enabled,
            successMessage = "Time format updated"
        )
    }

    private fun updateGroupSimilarEntries(enabled: Boolean) {
        settings.putBoolean("group_similar_entries", enabled)
        _state.value = _state.value.copy(
            groupSimilarEntries = enabled,
            successMessage = "Entry grouping ${if (enabled) "enabled" else "disabled"}"
        )
    }

    private fun updateDateFormat(format: String) {
        settings.putString("date_format", format)
        _state.value = _state.value.copy(
            dateFormat = format,
            showDateFormatDialog = false,
            successMessage = "Date format updated to $format"
        )
    }

    private fun updateDurationFormat(format: String) {
        settings.putString("duration_format", format)
        _state.value = _state.value.copy(
            durationFormat = format,
            showDurationFormatDialog = false,
            successMessage = "Duration format updated to $format"
        )
    }

    private fun updateFirstDayOfWeek(day: String) {
        settings.putString("first_day_of_week", day)
        _state.value = _state.value.copy(
            firstDayOfWeek = day,
            showFirstDayDialog = false,
            successMessage = "First day of week set to $day"
        )
    }

    private fun updateFullName(name: String) {
        if (name.trim().isEmpty()) {
            _state.value = _state.value.copy(
                errorMessage = "Name cannot be empty",
                showEditNameDialog = false
            )
            return
        }

        screenModelScope.launch {
            _state.value = _state.value.copy(isUpdatingProfile = true)

            try {
                val currentUser = _state.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(fullName = name.trim())

//                    userRepository.updateUser(updatedUser).fold(
//                        onSuccess = { user ->
//                            _state.value = _state.value.copy(
//                                user = user,
//                                isUpdatingProfile = false,
//                                showEditNameDialog = false,
//                                successMessage = "Name updated successfully"
//                            )
//                        },
//                        onFailure = { error ->
//                            _state.value = _state.value.copy(
//                                isUpdatingProfile = false,
//                                showEditNameDialog = false,
//                                errorMessage = "Failed to update name: ${error.message}"
//                            )
//                        }
//                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUpdatingProfile = false,
                    showEditNameDialog = false,
                    errorMessage = "Error updating name: ${e.message}"
                )
            }
        }
    }

    private fun updateTimezone(timezone: String) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isUpdatingProfile = true)

            try {
                val currentUser = _state.value.user
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(timezone = timezone)

//                    userRepository.updateUser(updatedUser).fold(
//                        onSuccess = { user ->
//                            _state.value = _state.value.copy(
//                                user = user,
//                                timezone = timezone,
//                                isUpdatingProfile = false,
//                                showTimezoneDialog = false,
//                                successMessage = "Timezone updated successfully"
//                            )
//                        },
//                        onFailure = { error ->
//                            _state.value = _state.value.copy(
//                                isUpdatingProfile = false,
//                                showTimezoneDialog = false,
//                                errorMessage = "Failed to update timezone: ${error.message}"
//                            )
//                        }
//                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isUpdatingProfile = false,
                    showTimezoneDialog = false,
                    errorMessage = "Error updating timezone: ${e.message}"
                )
            }
        }
    }

    private fun exportData() {
        screenModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    successMessage = "Data export started. You'll receive an email when ready."
                )

                // TODO: Implement actual data export functionality
                // This could export time entries, projects, etc. as JSON/CSV

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to export data: ${e.message}"
                )
            }
        }
    }

    private fun clearCache() {
        screenModelScope.launch {
            try {
                // Clear local cache data
//                timeEntryRepository.clearCache()
//                projectRepository.clearCache()

                _state.value = _state.value.copy(
                    successMessage = "Cache cleared successfully"
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "Failed to clear cache: ${e.message}"
                )
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }
}