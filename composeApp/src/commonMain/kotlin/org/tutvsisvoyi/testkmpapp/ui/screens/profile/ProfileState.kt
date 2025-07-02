package org.tutvsisvoyi.testkmpapp.ui.screens.profile

import org.tutvsisvoyi.testkmpapp.domain.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false,

    // Settings
    val use24HourFormat: Boolean = false,
    val groupSimilarEntries: Boolean = false,
    val dateFormat: String = "MM/DD/YYYY",
    val durationFormat: String = "Improved",
    val firstDayOfWeek: String = "Monday",
    val timezone: String = "",

    // Dialog states
    val showDateFormatDialog: Boolean = false,
    val showDurationFormatDialog: Boolean = false,
    val showFirstDayDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false
) {
    val isError: Boolean get() = errorMessage != null
}