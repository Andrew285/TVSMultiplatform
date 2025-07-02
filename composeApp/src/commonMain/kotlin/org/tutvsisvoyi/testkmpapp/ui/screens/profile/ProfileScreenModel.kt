package org.tutvsisvoyi.testkmpapp.ui.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tutvsisvoyi.testkmpapp.domain.repository.AuthRepository

class ProfileScreenModel(
    private val authRepository: AuthRepository,
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
            is ProfileAction.UpdateUse24HourFormat -> updateUse24HourFormat(action.enabled)
            is ProfileAction.UpdateGroupSimilarEntries -> updateGroupSimilarEntries(action.enabled)
            is ProfileAction.UpdateDateFormat -> updateDateFormat(action.format)
            is ProfileAction.UpdateDurationFormat -> updateDurationFormat(action.format)
            is ProfileAction.UpdateFirstDayOfWeek -> updateFirstDayOfWeek(action.day)

            // Dialog actions
            is ProfileAction.ShowDateFormatDialog -> _state.value = _state.value.copy(showDateFormatDialog = true)
            is ProfileAction.HideDateFormatDialog -> _state.value = _state.value.copy(showDateFormatDialog = false)
            is ProfileAction.ShowDurationFormatDialog -> _state.value = _state.value.copy(showDurationFormatDialog = true)
            is ProfileAction.HideDurationFormatDialog -> _state.value = _state.value.copy(showDurationFormatDialog = false)
            is ProfileAction.ShowFirstDayDialog -> _state.value = _state.value.copy(showFirstDayDialog = true)
            is ProfileAction.HideFirstDayDialog -> _state.value = _state.value.copy(showFirstDayDialog = false)
        }
    }

    private fun loadProfile() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _state.value = _state.value.copy(
                        user = user,
                        isLoading = false,
                        timezone = user.timezone
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "User not found"
                    )
                }
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
                authRepository.logout()
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
        _state.value = _state.value.copy(use24HourFormat = enabled)
    }

    private fun updateGroupSimilarEntries(enabled: Boolean) {
        settings.putBoolean("group_similar_entries", enabled)
        _state.value = _state.value.copy(groupSimilarEntries = enabled)
    }

    private fun updateDateFormat(format: String) {
        settings.putString("date_format", format)
        _state.value = _state.value.copy(
            dateFormat = format,
            showDateFormatDialog = false
        )
    }

    private fun updateDurationFormat(format: String) {
        settings.putString("duration_format", format)
        _state.value = _state.value.copy(
            durationFormat = format,
            showDurationFormatDialog = false
        )
    }

    private fun updateFirstDayOfWeek(day: String) {
        settings.putString("first_day_of_week", day)
        _state.value = _state.value.copy(
            firstDayOfWeek = day,
            showFirstDayDialog = false
        )
    }

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}