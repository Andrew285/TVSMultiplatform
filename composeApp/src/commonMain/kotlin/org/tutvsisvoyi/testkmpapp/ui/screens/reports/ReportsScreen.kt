package org.tutvsisvoyi.testkmpapp.ui.screens.reports

//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen

//class ProfileScreen : Screen {
//    @Composable
//    override fun Content() {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Profile Screen",
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.padding(bottom = 32.dp)
//            )
//
//            Card(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp)
//                ) {
//                    Text(
//                        text = "User Profile",
//                        style = MaterialTheme.typography.headlineSmall,
//                        modifier = Modifier.padding(bottom = 16.dp)
//                    )
//                    ProfileItem(label = "Name", value = "John Doe")
//                    ProfileItem(label = "Email", value = "john.doe@example.com")
//                    ProfileItem(label = "Location", value = "San Francisco, CA")
//                    ProfileItem(label = "Member since", value = "January 2024")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            ElevatedButton(
//                onClick = { /* Handle edit profile */ },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Edit Profile")
//            }
//        }
//    }
//}
//
//@Composable
//private fun ProfileItem(label: String, value: String) {
//    Column(
//        modifier = Modifier.padding(bottom = 12.dp)
//    ) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(
//            text = value,
//            style = MaterialTheme.typography.bodyLarge,
//            modifier = Modifier.padding(top = 2.dp)
//        )
//    }
//}

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import io.ktor.http.ContentDisposition
import kotlinx.datetime.LocalDate
import org.tutvsisvoyi.testkmpapp.data.network.savePdfToDownloads
import org.tutvsisvoyi.testkmpapp.data.utils.CalendarUtils
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                "Loading reports...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoDataContent(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Lucide.ChartBar,
                contentDescription = "No data",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "No data for selected period",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "Try adjusting your date range or start tracking time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun ReportTypeSelector(
    currentType: ReportType,
    onTypeSelected: (ReportType) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(ReportType.values()) { type ->
            FilterChip(
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        when (type) {
                            ReportType.DAILY -> "Today"
                            ReportType.WEEKLY -> "This Week"
                            ReportType.MONTHLY -> "This Month"
                            ReportType.CUSTOM -> "Custom"
                        }
                    )
                },
                selected = currentType == type,
                leadingIcon = {
                    Icon(
                        when (type) {
                            ReportType.DAILY -> Lucide.Calendar
                            ReportType.WEEKLY -> Lucide.CalendarDays
                            ReportType.MONTHLY -> Lucide.CalendarRange
                            ReportType.CUSTOM -> Lucide.Settings
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SummaryCards(chartData: ChartData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Hours",
            value = "${chartData.totalHours.getDigits(2)}h",
            icon = Lucide.Clock,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Days Worked",
            value = chartData.totalDays.toString(),
            icon = Lucide.Calendar,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Avg/Day",
            value = "${chartData.averageHoursPerDay.getDigits(2)}h",
            icon = Lucide.TrendingUp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DailyHoursChart(dailyHours: List<DayHours>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Lucide.ChartBar,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Daily Hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dailyHours.isEmpty()) {
                Text(
                    "No data available",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Simple bar chart implementation
                val maxHours = dailyHours.maxOfOrNull { it.hours } ?: 0.0

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(dailyHours.take(10)) { dayData ->
                        DailyHoursBarItem(
                            dayData = dayData,
                            maxHours = maxHours
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyHoursBarItem(
    dayData: DayHours,
    maxHours: Double
) {
    val fillRatio = if (maxHours > 0) (dayData.hours / maxHours).toFloat() else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDateShort(dayData.date),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(60.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillRatio)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            Text(
                text = "${dayData.hours.getDigits(2)}h",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun ProjectHoursChart(projectHours: List<ProjectHours>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Lucide.ChartPie,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Hours by Project",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (projectHours.isEmpty()) {
                Text(
                    "No project data available",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(projectHours) { project ->
                        ProjectHoursItem(projectData = project)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectHoursItem(projectData: ProjectHours) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    Color(Color.Red.value),
                    RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = projectData.projectName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${projectData.entries.size} entries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${projectData.hours.getDigits(2)}h",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            state = state
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier.pullToRefresh(state = state, isRefreshing = isRefreshing, onRefresh = onRefresh),
        contentAlignment = contentAlignment
    ) {
        content()
        indicator()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ReportsState,
    onAction: (ReportsAction) -> Unit
) {
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Handle PDF saving when bytes become available
    LaunchedEffect(state.generatedPdfBytes) {
        state.generatedPdfBytes?.let { pdfBytes ->
            state.generatedFileName?.let { fileName ->
                try {
                    val result = savePdfToDownloads(pdfBytes, fileName)
                    result.fold(
                        onSuccess = { filePath ->
                            saveMessage = "PDF saved successfully to: $filePath"
                            isSuccess = true
                            println("PDF saved: $filePath")
                        },
                        onFailure = { error ->
                            saveMessage = "Failed to save PDF: ${error.message}"
                            isSuccess = false
                            println("PDF save failed: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    saveMessage = "Error saving PDF: ${e.message}"
                    isSuccess = false
                }
            }
        }
    }

    // Clear message after 5 seconds
    LaunchedEffect(saveMessage) {
        saveMessage?.let {
            kotlinx.coroutines.delay(5000)
            saveMessage = null
        }
    }

    // Clear email message after 5 seconds
    LaunchedEffect(state.emailSendMessage) {
        state.emailSendMessage?.let {
            kotlinx.coroutines.delay(5000)
            onAction(ReportsAction.ClearEmailMessage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "Reports",
                    fontWeight = FontWeight.Medium
                )
            },
            actions = {
                // Send by Email button - only show if PDF is generated
                if (state.hasPdfGenerated) {
                    IconButton(
                        onClick = { onAction(ReportsAction.ShowEmailDialog) },
                        enabled = !state.isSendingEmail
                    ) {
                        if (state.isSendingEmail) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Lucide.Mail, contentDescription = "Send by Email")
                        }
                    }
                }

                // Generate PDF button
                IconButton(
                    onClick = { onAction(ReportsAction.GeneratePdfReport) },
                    enabled = !state.isGeneratingPdf && state.hasData
                ) {
                    if (state.isGeneratingPdf) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.FileText, contentDescription = "Generate PDF")
                    }
                }

                // Refresh button
                IconButton(
                    onClick = { onAction(ReportsAction.RefreshReports) },
                    enabled = !state.isRefreshing && state.hasWorkspace
                ) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.RefreshCw, contentDescription = "Refresh")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        saveMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (isSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { saveMessage = null }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Email send message
        state.emailSendMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isEmailSendSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = if (state.isEmailSendSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAction(ReportsAction.ClearEmailMessage) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Error message for reports
        if (state.isError) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAction(ReportsAction.ClearError) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ReportsAction.RefreshReports) },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }

                !state.hasData -> {
                    NoDataContent(
                        onRefresh = { onAction(ReportsAction.RefreshReports) }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            ReportTypeSelector(
                                currentType = state.reportType,
                                onTypeSelected = { type ->
                                    if (type == ReportType.CUSTOM) {
                                        showDateRangeDialog = true
                                    }
                                    onAction(ReportsAction.ChangeReportType(type))
                                }
                            )
                        }

                        item {
                            // Date range display
                            if (state.startDate != null && state.endDate != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .clickable { showDateRangeDialog = true },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Lucide.Calendar,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${formatDateShort(state.startDate)} - ${formatDateShort(state.endDate)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        Lucide.ChevronRight,
                                        contentDescription = "Change dates",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            SummaryCards(state.chartData)
                        }

                        item {
                            DailyHoursChart(state.chartData.dailyHours)
                        }

                        item {
                            ProjectHoursChart(state.chartData.projectHours)
                        }
                    }
                }
            }
        }

        // Date Range Picker Dialog
        if (showDateRangeDialog) {
            val dateRangePickerState = rememberDateRangePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return true
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        val currentYear = CalendarUtils.today().year
                        return year >= currentYear - 5
                    }
                }
            )

            BasicAlertDialog(
                onDismissRequest = {
                    showDateRangeDialog = false
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        DateRangePicker(
                            state = dateRangePickerState,
                            modifier = Modifier.fillMaxWidth(),
                            title = {
                                Text(
                                    "Select Date Range",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showDateRangeDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val startDate = dateRangePickerState.selectedStartDateMillis?.let {
                                        CalendarUtils.millisToLocalDate(it)
                                    }

                                    val endDate = dateRangePickerState.selectedEndDateMillis?.let {
                                        CalendarUtils.millisToLocalDate(it)
                                    }

                                    onAction(ReportsAction.SelectDateRange(startDate?.toString(), endDate?.toString()))
                                    onAction(ReportsAction.ChangeReportType(ReportType.CUSTOM))
                                    showDateRangeDialog = false
                                }
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }

        // Email PDF Dialog
        EmailPdfDialog(
            isVisible = state.showEmailDialog,
            fileName = state.generatedFileName,
            reportType = state.reportType,
            startDate = state.startDate,
            endDate = state.endDate,
            isSending = state.isSendingEmail,
            onDismiss = { onAction(ReportsAction.HideEmailDialog) },
            onSend = { email, subject, body ->
                onAction(ReportsAction.SendPdfByEmail(email, subject, body))
            }
        )
    }
}

fun formatDateShort(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        "${date.month.name.take(3)} ${date.dayOfMonth}"
    } catch (e: Exception) {
        dateString
    }
}

private fun Double.getDigits(digitsCount: Int): Double {
    val factor = 10.0.pow(digitsCount)
    return kotlin.math.floor(this * factor) / factor
}


