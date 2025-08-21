package org.tutvsisvoyi.testkmpapp.ui.screens.profile

sealed class ProfileAction {
    object LoadProfile : ProfileAction()
    object Logout : ProfileAction()
    object ConfirmLogout : ProfileAction()
    object CancelLogout : ProfileAction()
    object ClearError : ProfileAction()
    object ClearSuccessMessage : ProfileAction()

    // Settings updates
    data class UpdateUse24HourFormat(val enabled: Boolean) : ProfileAction()
    data class UpdateGroupSimilarEntries(val enabled: Boolean) : ProfileAction()
    data class UpdateDateFormat(val format: String) : ProfileAction()
    data class UpdateDurationFormat(val format: String) : ProfileAction()
    data class UpdateFirstDayOfWeek(val day: String) : ProfileAction()

    // Profile updates
    data class UpdateFullName(val name: String) : ProfileAction()
    data class UpdateTimezone(val timezone: String) : ProfileAction()

    // Account actions
    object ExportData : ProfileAction()
    object ClearCache : ProfileAction()

    // Dialog actions for existing settings
    object ShowDateFormatDialog : ProfileAction()
    object HideDateFormatDialog : ProfileAction()
    object ShowDurationFormatDialog : ProfileAction()
    object HideDurationFormatDialog : ProfileAction()
    object ShowFirstDayDialog : ProfileAction()
    object HideFirstDayDialog : ProfileAction()

    // Dialog actions for new features
    object ShowEditNameDialog : ProfileAction()
    object HideEditNameDialog : ProfileAction()
    object ShowTimezoneDialog : ProfileAction()
    object HideTimezoneDialog : ProfileAction()
}