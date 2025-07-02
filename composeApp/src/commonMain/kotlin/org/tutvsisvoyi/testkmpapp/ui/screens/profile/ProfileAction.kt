package org.tutvsisvoyi.testkmpapp.ui.screens.profile

sealed class ProfileAction {
    object LoadProfile : ProfileAction()
    object Logout : ProfileAction()
    object ConfirmLogout : ProfileAction()
    object CancelLogout : ProfileAction()
    object ClearError : ProfileAction()
    data class UpdateUse24HourFormat(val enabled: Boolean) : ProfileAction()
    data class UpdateGroupSimilarEntries(val enabled: Boolean) : ProfileAction()
    data class UpdateDateFormat(val format: String) : ProfileAction()
    data class UpdateDurationFormat(val format: String) : ProfileAction()
    data class UpdateFirstDayOfWeek(val day: String) : ProfileAction()

    // Dialog actions
    object ShowDateFormatDialog : ProfileAction()
    object HideDateFormatDialog : ProfileAction()
    object ShowDurationFormatDialog : ProfileAction()
    object HideDurationFormatDialog : ProfileAction()
    object ShowFirstDayDialog : ProfileAction()
    object HideFirstDayDialog : ProfileAction()
}