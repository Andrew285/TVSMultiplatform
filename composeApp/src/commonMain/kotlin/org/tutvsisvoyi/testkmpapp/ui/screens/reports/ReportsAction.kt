package org.tutvsisvoyi.testkmpapp.ui.screens.reports

sealed class ReportsAction {
    object LoadReports : ReportsAction()
    object RefreshReports : ReportsAction()
    object SyncReports : ReportsAction()
    object ClearError : ReportsAction()
    object LoadProjects : ReportsAction()
    data class SelectDateRange(val startDate: String?, val endDate: String?) : ReportsAction()
    data class ChangeReportType(val reportType: ReportType) : ReportsAction()
    object GeneratePdfReport : ReportsAction()
    object ShowDateRangePicker : ReportsAction()
    object ShowEmailDialog : ReportsAction()
    object HideEmailDialog : ReportsAction()
    data class SendPdfByEmail(val email: String, val subject: String, val body: String) : ReportsAction()
    object ClearEmailMessage : ReportsAction()
}