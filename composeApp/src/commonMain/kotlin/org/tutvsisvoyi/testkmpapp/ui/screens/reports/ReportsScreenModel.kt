package org.tutvsisvoyi.testkmpapp.ui.screens.reports

import TogglPdfExporter
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import androidx.compose.material3.ExperimentalMaterial3Api
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.tutvsisvoyi.testkmpapp.data.network.TogglApiClient
import org.tutvsisvoyi.testkmpapp.data.utils.CalendarUtils
import org.tutvsisvoyi.testkmpapp.domain.repository.ProjectRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.TimeEntryRepository
import org.tutvsisvoyi.testkmpapp.domain.repository.WorkspaceRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
class ReportsScreenModel(
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val projectsRepository: ProjectRepository,
    private val togglApiClient: TogglApiClient,
) : ScreenModel {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    private var currentStartDate: String? = CalendarUtils.firstDayOfThisMonth().toString()
    private var currentEndDate: String? = CalendarUtils.today().toString()
    private var reportsJob: Job? = null
    private val pdfExporter = TogglPdfExporter(togglApiClient)

    init {
        initializeScreen()
    }

    private fun initializeScreen() {
        screenModelScope.launch {
            val currentWorkspace = workspaceRepository.getCurrentWorkspace()
            if (currentWorkspace != null) {
                _state.value = _state.value.copy(
                    currentWorkspaceId = currentWorkspace.id,
                    startDate = currentStartDate,
                    endDate = currentEndDate
                )
                startReportsCollection()
                loadProjects()
            } else {
                workspaceRepository.syncWorkspaces().fold(
                    onSuccess = { workspaces ->
                        if (workspaces.isNotEmpty()) {
                            val firstWorkspace = workspaces.first()
                            workspaceRepository.setCurrentWorkspace(firstWorkspace)
                            _state.value = _state.value.copy(
                                currentWorkspaceId = firstWorkspace.id,
                                startDate = currentStartDate,
                                endDate = currentEndDate
                            )
                            startReportsCollection()
                            loadProjects()
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

    private fun startReportsCollection() {
        val workspaceId = _state.value.currentWorkspaceId ?: return

        reportsJob?.cancel()
        reportsJob = screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                timeEntryRepository.getTimeEntries(
                    workspaceId = workspaceId,
                    startDate = null, // currentStartDate
                    endDate = null, // currentEndDate
                ).collect { entries ->
                    val chartData = calculateChartData(entries)
                    _state.value = _state.value.copy(
                        timeEntries = entries,
                        isLoading = false,
                        chartData = chartData,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load reports: ${e.message}"
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateChartData(entries: List<org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry>): ChartData {
        if (entries.isEmpty()) return ChartData()

        // Group by date for daily hours
        val dailyHours = entries
            .filter { it.stop != null } // Only completed entries
            .groupBy {
                it.start.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.toString()
            }
            .map { (date, entriesForDate) ->
                val totalHours = entriesForDate.sumOf { entry ->
                    if (entry.stop != null) {
                        (entry.stop.toEpochMilliseconds() - entry.start.toEpochMilliseconds()).milliseconds.inWholeHours.toDouble() +
                                ((entry.stop.toEpochMilliseconds() - entry.start.toEpochMilliseconds()).milliseconds.inWholeMinutes % 60) / 60.0
                    } else 0.0
                }
                DayHours(date, totalHours, entriesForDate)
            }
            .sortedBy { it.date }

        // Group by project for project hours
        val projectHours = entries
            .filter { it.stop != null }
            .groupBy { it.project?.name ?: "No Project" }
            .map { (projectName, entriesForProject) ->
                val totalHours = entriesForProject.sumOf { entry ->
                    if (entry.stop != null) {
                        (entry.stop.toEpochMilliseconds() - entry.start.toEpochMilliseconds()).milliseconds.inWholeHours.toDouble() +
                                ((entry.stop.toEpochMilliseconds() - entry.start.toEpochMilliseconds()).milliseconds.inWholeMinutes % 60) / 60.0
                    } else 0.0
                }
                val color = entriesForProject.firstOrNull()?.project?.color ?: "#3498db"
                ProjectHours(projectName, totalHours, color, entriesForProject)
            }
            .sortedByDescending { it.hours }

        val totalHours = dailyHours.sumOf { it.hours }
        val totalDays = dailyHours.size
        val averageHoursPerDay = if (totalDays > 0) totalHours / totalDays else 0.0

        return ChartData(
            dailyHours = dailyHours,
            projectHours = projectHours,
            totalHours = totalHours,
            totalDays = totalDays,
            averageHoursPerDay = averageHoursPerDay
        )
    }

    private fun refreshReports() {
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

    private fun selectDateRange(startDate: String?, endDate: String?) {
        currentStartDate = startDate
        currentEndDate = endDate
        _state.value = _state.value.copy(
            startDate = startDate,
            endDate = endDate
        )
        startReportsCollection()
    }

    private fun changeReportType(reportType: ReportType) {
        val today = CalendarUtils.today()
        val (start, end) = when (reportType) {
            ReportType.DAILY -> {
                today.toString() to today.toString()
            }
            ReportType.WEEKLY -> {
                val startOfWeek = today.minus(today.dayOfWeek.ordinal, kotlinx.datetime.DateTimeUnit.DAY)
                startOfWeek.toString() to today.toString()
            }
            ReportType.MONTHLY -> {
                CalendarUtils.firstDayOfThisMonth().toString() to today.toString()
            }
            ReportType.CUSTOM -> {
                // Keep current dates for custom
                currentStartDate to currentEndDate
            }
        }

        _state.value = _state.value.copy(reportType = reportType)
        if (reportType != ReportType.CUSTOM) {
            selectDateRange(start, end)
        }
    }

//    private fun generatePdfReport() {
//        screenModelScope.launch {
//            _state.value = _state.value.copy(isGeneratingPdf = true, errorMessage = null)
//
//            try {
//                // TODO: Implement PDF generation
//                // This would involve creating a PDF with charts and data
//                kotlinx.coroutines.delay(2000) // Simulate PDF generation
//
//                _state.value = _state.value.copy(
//                    isGeneratingPdf = false,
//                    errorMessage = null
//                )
//
//                // Show success message or save file
//                println("PDF report generated successfully!")
//
//            } catch (e: Exception) {
//                _state.value = _state.value.copy(
//                    isGeneratingPdf = false,
//                    errorMessage = "Failed to generate PDF: ${e.message}"
//                )
//            }
//        }
//    }
private fun generateTogglPdfReport() {
    screenModelScope.launch {
        _state.value = _state.value.copy(isGeneratingPdf = true, errorMessage = null)

        val workspaceId = _state.value.currentWorkspaceId
        if (workspaceId == null) {
            _state.value = _state.value.copy(
                isGeneratingPdf = false,
                errorMessage = "No workspace selected"
            )
            return@launch
        }

        try {
            val result = when (_state.value.reportType) {
                ReportType.DAILY, ReportType.CUSTOM -> {
                    pdfExporter.exportDetailedReportPdf( // Use the working minimal version
                        workspaceId = workspaceId,
                        startDate = _state.value.startDate!!,
                        endDate = _state.value.endDate!!
                    )
                }
                ReportType.WEEKLY, ReportType.MONTHLY -> {
                    pdfExporter.exportSummaryReportPdf(
                        workspaceId = workspaceId,
                        startDate = _state.value.startDate!!,
                        endDate = _state.value.endDate!!,
                        grouping = "projects"
                    )
                }
            }

            result.fold(
                onSuccess = { pdfBytes ->
                    val fileName = generateFileName(_state.value.reportType, _state.value.startDate, _state.value.endDate)

                    // Store PDF data in state instead of saving to file
                    _state.value = _state.value.copy(
                        isGeneratingPdf = false,
                        generatedPdfBytes = pdfBytes,
                        generatedFileName = fileName,
                        errorMessage = null
                    )

                    println("PDF generated successfully: ${pdfBytes.size} bytes")
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isGeneratingPdf = false,
                        errorMessage = "Failed to generate PDF: ${error.message}"
                    )
                }
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isGeneratingPdf = false,
                errorMessage = "Failed to generate PDF: ${e.message}"
            )
        }
    }
}

    private fun clearGeneratedPdf() {
        _state.value = _state.value.copy(
            generatedPdfBytes = null,
            generatedFileName = null
        )
    }

//    private suspend fun savePdfToFile(pdfBytes: ByteArray, fileName: String): String {
//        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
//        file.writeBytes(pdfBytes)
//        return file.absolutePath
//    }

    @OptIn(ExperimentalTime::class)
    private fun generateFileName(reportType: ReportType, startDate: String?, endDate: String?): String {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateString = "${currentDate.year}${currentDate.monthNumber.toString().padStart(2, '0')}${currentDate.dayOfMonth.toString().padStart(2, '0')}"

        return when (reportType) {
            ReportType.DAILY -> "toggl_daily_report_$dateString.pdf"
            ReportType.WEEKLY -> "toggl_weekly_report_$dateString.pdf"
            ReportType.MONTHLY -> "toggl_monthly_report_$dateString.pdf"
            ReportType.CUSTOM -> {
                val start = startDate?.replace("-", "") ?: dateString
                val end = endDate?.replace("-", "") ?: dateString
                "toggl_custom_report_${start}_to_$end.pdf"
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

    private fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun showEmailDialog() {
        _state.value = _state.value.copy(showEmailDialog = true)
    }

    private fun hideEmailDialog() {
        _state.value = _state.value.copy(showEmailDialog = false)
    }

    private fun sendPdfByEmail(email: String, subject: String, body: String) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isSendingEmail = true, emailSendMessage = null)

            val pdfBytes = _state.value.generatedPdfBytes
            val fileName = _state.value.generatedFileName

            if (pdfBytes == null || fileName == null) {
                _state.value = _state.value.copy(
                    isSendingEmail = false,
                    emailSendMessage = "No PDF available to send. Please generate a report first.",
                    isEmailSendSuccess = false
                )
                return@launch
            }

            try {
                // Call your email service here
                val result = sendEmailWithAttachment(
                    toEmail = email,
                    subject = subject,
                    body = body,
                    attachmentBytes = pdfBytes,
                    attachmentName = fileName,
                    attachmentMimeType = "application/pdf"
                )

                result.fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isSendingEmail = false,
                            showEmailDialog = false,
                            emailSendMessage = "Email sent successfully to $email",
                            isEmailSendSuccess = true
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isSendingEmail = false,
                            emailSendMessage = "Failed to send email: ${error.message}",
                            isEmailSendSuccess = false
                        )
                    }
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSendingEmail = false,
                    emailSendMessage = "Error sending email: ${e.message}",
                    isEmailSendSuccess = false
                )
            }
        }
    }

    private fun clearEmailMessage() {
        _state.value = _state.value.copy(emailSendMessage = null)
    }

    // Email service interface - you'll need to implement this based on your email provider
// This could be using a REST API, SMTP, or a third-party service like SendGrid, etc.
    private suspend fun sendEmailWithAttachment(
        toEmail: String,
        subject: String,
        body: String,
        attachmentBytes: ByteArray,
        attachmentName: String,
        attachmentMimeType: String
    ): Result<Unit> {
        return try {
            // Example implementation using a hypothetical email service
            // Replace this with your actual email service implementation

            // Option 1: Using a REST API email service (e.g., SendGrid, Mailgun, etc.)
            val emailRequest = EmailRequest(
                to = toEmail,
                subject = subject,
                htmlContent = body.replace("\n", "<br>"),
                textContent = body,
                attachments = listOf(
                    EmailAttachment(
                        content = attachmentBytes.encodeToBase64(),
                        filename = attachmentName,
                        type = attachmentMimeType
                    )
                )
            )

            // TODO; add email implementation
//            val response = togglApiClient.sendEmail(emailRequest) // You'll need to add this to your API clientval

//            if (response.isSuccessful) {
//                Result.success(Unit)
//            } else {
//                Result.failure(Exception("Email service error: ${response.errorBody?.string()}"))
//            }
            return Result.success(Unit)

            // Option 2: Using Android Intent for email (if on Android platform)
            // This would open the user's email app with the PDF attached
            /*
            if (Platform.isAndroid) {
                sendEmailViaIntent(toEmail, subject, body, attachmentBytes, attachmentName)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Email not supported on this platform"))
            }
            */

            // Option 3: Using platform-specific email implementations
            // You could create expect/actual functions for different platforms

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun handleAction(action: ReportsAction) {
        when (action) {
            is ReportsAction.LoadReports -> startReportsCollection()
            is ReportsAction.RefreshReports -> refreshReports()
            is ReportsAction.SyncReports -> refreshReports()
            is ReportsAction.ClearError -> clearError()
            is ReportsAction.SelectDateRange -> selectDateRange(action.startDate, action.endDate)
            is ReportsAction.ChangeReportType -> changeReportType(action.reportType)
            is ReportsAction.GeneratePdfReport -> generateTogglPdfReport()
            is ReportsAction.LoadProjects -> loadProjects()
            is ReportsAction.ShowDateRangePicker -> {
                // Handle showing date range picker in UI
            }
            is ReportsAction.ShowEmailDialog -> showEmailDialog()
            is ReportsAction.HideEmailDialog -> hideEmailDialog()
            is ReportsAction.SendPdfByEmail -> sendPdfByEmail(action.email, action.subject, action.body)
            is ReportsAction.ClearEmailMessage -> clearEmailMessage()
        }
    }

    override fun onDispose() {
        super.onDispose()
        reportsJob?.cancel()
    }
}

// Data classes for email service API
data class EmailRequest(
    val to: String,
    val subject: String,
    val htmlContent: String,
    val textContent: String,
    val attachments: List<EmailAttachment>
)

data class EmailAttachment(
    val content: String, // Base64 encoded
    val filename: String,
    val type: String
)

// Extension function to encode ByteArray to Base64
private fun ByteArray.encodeToBase64(): String {
    // You'll need to implement this based on your platform
    // For KMP, you might use kotlinx-serialization or platform-specific implementations
    return "" // Placeholder - implement based on your Base64 encoding solution
}