package org.tutvsisvoyi.testkmpapp.ui.screens.reports

import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

data class ReportsState @OptIn(ExperimentalMaterial3Api::class) constructor(
    val timeEntries: List<TimeEntry> = emptyList(),
    val projects: List<Project> = emptyList(),
    val selectedDateRange: DateRangePickerState? = null,
    val currentWorkspaceId: Long? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val errorMessage: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val reportType: ReportType = ReportType.DAILY,
    val chartData: ChartData = ChartData(),
    val generatedPdfBytes: ByteArray? = null,
    val generatedFileName: String? = null
) {
    val isError: Boolean get() = errorMessage != null
    val hasWorkspace: Boolean get() = currentWorkspaceId != null
    val hasData: Boolean get() = timeEntries.isNotEmpty()
}

enum class ReportType {
    DAILY, WEEKLY, MONTHLY, CUSTOM
}

data class ChartData(
    val dailyHours: List<DayHours> = emptyList(),
    val projectHours: List<ProjectHours> = emptyList(),
    val totalHours: Double = 0.0,
    val totalDays: Int = 0,
    val averageHoursPerDay: Double = 0.0
)

data class DayHours(
    val date: String,
    val hours: Double,
    val entries: List<TimeEntry>
)

data class ProjectHours(
    val projectName: String,
    val hours: Double,
    val color: String,
    val entries: List<TimeEntry>
)