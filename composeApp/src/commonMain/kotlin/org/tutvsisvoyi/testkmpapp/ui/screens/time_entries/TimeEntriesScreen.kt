package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.FolderSync
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import org.tutvsisvoyi.testkmpapp.data.utils.CalendarUtils
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.Tag
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
                "Loading time entries...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoWorkspaceContent(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "No workspace found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Please check your account settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "No time entries found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Start tracking your time to see entries here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@ExperimentalMaterial3Api
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

@OptIn(ExperimentalTime::class)
@Composable
private fun TimeEntriesList(
    timeEntries: List<TimeEntry>,
    onTimeEntryClicked: (TimeEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Group entries by date
        val groupedEntries = timeEntries
            .sortedByDescending { it.start }
            .groupBy {
                it.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }

        groupedEntries.forEach { (date, entriesForDate) ->
            item {
                DateHeader(date = date)
            }

            items(entriesForDate) { timeEntry ->
                TimeEntryItem(
                    timeEntry = timeEntry,
                    onTimeEntryClicked = onTimeEntryClicked
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DateHeader(date: LocalDate) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    val dateText = when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
    }

    Text(
        text = dateText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun TimeEntryItem(
    timeEntry: TimeEntry,
    onTimeEntryClicked: (TimeEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onTimeEntryClicked(timeEntry)
           },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Description and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = timeEntry.description!!.ifEmpty { "No description" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Running indicator
                if (timeEntry.stop == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
//                        Icon(
//                            ImageBitmap(0, 0),
//                            contentDescription = "Running",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(16.dp)
//                        )
                        Text(
                            "Running",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    // Start time
                    Text(
                        text = formatTime(timeEntry.start),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // End time or "Running"
                    Text(
                        text = if (timeEntry.stop != null) {
                            formatTime(timeEntry.stop)
                        } else {
                            "Running..."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (timeEntry.stop != null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Duration
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formatDuration(timeEntry),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tags if any
            if (timeEntry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeEntry.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (timeEntry.tags.size > 3) {
                        Text(
                            "+${timeEntry.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatTime(instant: Instant): String {
    val timezone = TimeZone.of("Europe/Kiev")
    val localDateTime = instant.toLocalDateTime(timezone)
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
private fun formatDuration(timeEntry: TimeEntry): String {
    val duration = if (timeEntry.stop != null) {
        (timeEntry.stop.toEpochMilliseconds() - timeEntry.start.toEpochMilliseconds()).milliseconds
    } else {
        // For running entries, calculate current duration
        (Clock.System.now().toEpochMilliseconds() - timeEntry.start.toEpochMilliseconds()).milliseconds
    }

    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)
    val seconds = (duration.inWholeSeconds % 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeEntriesScreen(
    state: TimeEntriesState,
    onAction: (TimeEntriesAction) -> Unit
) {
    var showTimeTrackerDialog by remember { mutableStateOf(false) }
    var selectedTimeEntry by remember { mutableStateOf<TimeEntry?>(null) }
    var showTimeEntriesDateRangeDialog by remember { mutableStateOf(false) }
    var selectedTimeEntriesDateRange by remember { mutableStateOf<SelectableDates?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar WITHOUT back button
        TopAppBar(
            title = {
                Text(
                    "Time Entries",
                    fontWeight = FontWeight.Medium
                )
            },
            actions = {
                // Sync button
                IconButton(
                    onClick = { onAction(TimeEntriesAction.SyncTimeEntries) },
                    enabled = !state.isSyncing && state.hasWorkspace
                ) {
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Lucide.FolderSync, contentDescription = "Sync")
                    }
                }

                // Refresh button
                IconButton(
                    onClick = { onAction(TimeEntriesAction.RefreshTimeEntries) },
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

        // Error message
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
                        onClick = { onAction(TimeEntriesAction.ClearError) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(TimeEntriesAction.RefreshTimeEntries) },
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    shape = RoundedCornerShape(10.dp),
                    content = {
                        Text("BASquare")
                    },
                    onClick = {

                    }
                )

                OutlinedButton(
                    shape = RoundedCornerShape(10.dp),
                    content = {
                        Text("Date")
                    },
                    onClick = {
                        showTimeEntriesDateRangeDialog = true
                    }
                )
            }

            when {
                state.isLoading -> {
                    LoadingContent()
                }

                !state.hasWorkspace -> {
                    NoWorkspaceContent(
                        onRetry = {
                            onAction(TimeEntriesAction.LoadTimeEntries)
                        }
                    )
                }

                state.isEmpty -> {
                    EmptyContent()
                }

                else -> {
                    TimeEntriesList(
                        timeEntries = state.timeEntries,
                        onTimeEntryClicked = { timeEntry ->
                            selectedTimeEntry =  timeEntry
                            showTimeTrackerDialog = true
                        }
                    )
                }
            }
        }

        if (showTimeTrackerDialog) {
            BottomSlidingTrackerDialog(
                timeEntry = selectedTimeEntry,
                visible = showTimeTrackerDialog,
                projects = state.projects,
                tags = state.tags,
                onCreateTimeEntry = {
                    onAction(
                        TimeEntriesAction.CreateTimeEntry(
                            description = selectedTimeEntry?.description ?: "",
                            projectId = selectedTimeEntry?.projectId,
                            taskId = null,
                            tags = selectedTimeEntry?.tags ?: emptyList(),
                            billable = false,
                            workspaceId = selectedTimeEntry?.workspaceId!!
                        )
                    )
                },
                onDismissRequest = {
                    showTimeTrackerDialog = false
                },
                onSaveRequest = {

                }
            )
        }

        if (showTimeEntriesDateRangeDialog) {
            val dateRangePickerState = rememberDateRangePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return true
                    }

                    override fun isSelectableYear(year: Int): Boolean {
                        val currentYear = CalendarUtils.today().year
                        return year >= currentYear - 5 // Allow selection from 5 years ago
                    }
                }
            )

            BasicAlertDialog(
                onDismissRequest = {
                    showTimeEntriesDateRangeDialog = false
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
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showTimeEntriesDateRangeDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val startDate = dateRangePickerState.selectedStartDateMillis?.let {
                                        CalendarUtils.millisToLocalDate(
                                            it
                                        )
                                    }

                                    val endDate = dateRangePickerState.selectedEndDateMillis?.let {
                                        CalendarUtils.millisToLocalDate(
                                            it
                                        )
                                    }

                                    onAction(TimeEntriesAction.SelectDateRange(startDate.toString(), endDate.toString()))
                                    showTimeEntriesDateRangeDialog = false
                                }
                            ) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSlidingTrackerDialog(
    timeEntry: TimeEntry?,
    projects: List<Project>?,
    tags: List<Tag>?,
    visible: Boolean,
    onCreateTimeEntry: () -> Unit,
    onDismissRequest: () -> Unit,
    onSaveRequest: () -> Unit,
) {
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var selectedTags by remember { mutableStateOf<List<String>?>(null) }

    if (visible && timeEntry != null) {
        selectedProject = timeEntry.project
        selectedTags = timeEntry.tags

        ModalBottomSheet(
            shape = RoundedCornerShape(10.dp),
            dragHandle = {},
            onDismissRequest = onDismissRequest,
            content = {
                TimeTrackerContent(
                    timeEntry,
                    projects = projects,
                    tags = tags,
                    selectedProject = selectedProject,
                    selectedTags = selectedTags,
                    onProjectSelected = { project ->
                        selectedProject = project
                    },
                    onTagSelected = { tagName ->
                        if (selectedTags?.contains(tagName) == false) {
                            selectedTags = selectedTags?.plus(tagName)
                        }
                    },
                    onStartTimer = onCreateTimeEntry,
                    onSave = onSaveRequest
                )
            }
        )
    }
}