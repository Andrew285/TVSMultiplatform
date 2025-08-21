package org.tutvsisvoyi.testkmpapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.*
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ProfileScrollContent(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Section
        ProfileSection(
            user = state.user,
            onAction = onAction
        )

        // Date and Time Section
        DateTimeSection(
            use24HourFormat = state.use24HourFormat,
            dateFormat = state.dateFormat,
            durationFormat = state.durationFormat,
            firstDayOfWeek = state.firstDayOfWeek,
            groupSimilarEntries = state.groupSimilarEntries,
            onAction = onAction
        )

        // Account Actions
        AccountActionsSection(
            isLoggingOut = state.isLoggingOut,
            onAction = onAction
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileSection(
    user: org.tutvsisvoyi.testkmpapp.domain.model.User?,
    onAction: (ProfileAction) -> Unit
) {
    SectionHeader("YOUR PROFILE")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Personal details and preferences",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            if (user != null) {
                // Editable Full Name
                EditableProfileItem(
                    label = "Full name",
                    value = user.fullName.ifEmpty { "Not set" },
                    icon = Lucide.User,
                    onClick = { onAction(ProfileAction.ShowEditNameDialog) }
                )

                // Non-editable Email
                ProfileDetailItem(
                    label = "Email",
                    value = user.email,
                    icon = Lucide.Mail
                )

                // Editable Timezone
                EditableProfileItem(
                    label = "Reports Timezone",
                    value = if (user.timezone.isNotEmpty()) {
                        formatTimezone(user.timezone)
                    } else {
                        "Not set"
                    },
                    icon = Lucide.Globe,
                    onClick = { onAction(ProfileAction.ShowTimezoneDialog) }
                )

                // Integration Status
                ProfileDetailItem(
                    label = "Google Sign-In",
                    value = "Enabled",
                    icon = Lucide.Chrome
                )

                ProfileDetailItem(
                    label = "Apple Sign-In",
                    value = "Not Enabled",
                    icon = Lucide.Apple
                )
            }
        }
    }
}

@Composable
private fun DateTimeSection(
    use24HourFormat: Boolean,
    dateFormat: String,
    durationFormat: String,
    firstDayOfWeek: String,
    groupSimilarEntries: Boolean,
    onAction: (ProfileAction) -> Unit
) {
    SectionHeader("DATE AND TIME")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsItem(
                title = "Date format",
                subtitle = "${dateFormat} (${getDateExample(dateFormat)})",
                icon = Lucide.Calendar,
                onClick = { onAction(ProfileAction.ShowDateFormatDialog) }
            )

            SettingsToggleItem(
                title = "Use 24-hour clock",
                subtitle = "Display time in 24-hour format",
                icon = Lucide.Clock,
                checked = use24HourFormat,
                onCheckedChange = { onAction(ProfileAction.UpdateUse24HourFormat(it)) }
            )

            SettingsItem(
                title = "Duration format",
                subtitle = "$durationFormat (${getDurationExample(durationFormat)})",
                icon = Lucide.Timer,
                onClick = { onAction(ProfileAction.ShowDurationFormatDialog) }
            )

            SettingsItem(
                title = "First day of the week",
                subtitle = firstDayOfWeek,
                icon = Lucide.CalendarDays,
                onClick = { onAction(ProfileAction.ShowFirstDayDialog) }
            )

            SettingsToggleItem(
                title = "Group similar time entries",
                subtitle = "Combine entries with same project and description",
                icon = Lucide.Group,
                checked = groupSimilarEntries,
                onCheckedChange = { onAction(ProfileAction.UpdateGroupSimilarEntries(it)) }
            )
        }
    }
}

@Composable
private fun AccountActionsSection(
    isLoggingOut: Boolean,
    onAction: (ProfileAction) -> Unit
) {
    SectionHeader("ACCOUNT")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Management
            SettingsItem(
                title = "Export Data",
                subtitle = "Download your time tracking data",
                icon = Lucide.Download,
                onClick = { onAction(ProfileAction.ExportData) }
            )

            SettingsItem(
                title = "Clear Cache",
                subtitle = "Clear locally stored data",
                icon = Lucide.Trash2,
                onClick = { onAction(ProfileAction.ClearCache) }
            )

            Divider()

            // Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isLoggingOut) {
                            onAction(ProfileAction.Logout)
                        }
                    }
                    .padding(vertical = 62.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Lucide.LogOut,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Sign out of your account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Icon(
                        imageVector = Lucide.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ProfileDetailItem(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EditableProfileItem(
    label: String,
    value: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: ImageVector? = null,
    showLoading: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun formatTimezone(timezone: String): String {
    return when {
        timezone.contains("Europe/Kiev") || timezone.contains("Europe/Kyiv") -> "(GMT+02:00) Europe/Kyiv"
        timezone.contains("UTC") -> "(GMT+00:00) UTC"
        timezone.contains("America/New_York") -> "(GMT-05:00) America/New_York"
        timezone.contains("America/Los_Angeles") -> "(GMT-08:00) America/Los_Angeles"
        else -> timezone
    }
}

private fun getDateExample(format: String): String {
    return when (format) {
        "MM/DD/YYYY" -> "12/25/2024"
        "DD/MM/YYYY" -> "25/12/2024"
        "YYYY-MM-DD" -> "2024-12-25"
        "DD.MM.YYYY" -> "25.12.2024"
        "MMM DD, YYYY" -> "Dec 25, 2024"
        else -> "25/12/2024"
    }
}

private fun getDurationExample(format: String): String {
    return when (format) {
        "Improved" -> "0:47:06"
        "Classic" -> "0.78h"
        "Decimal" -> "47.1 min"
        else -> "0:47:06"
    }
}

// New Dialogs for editing profile information

@Composable
private fun EditNameDialog(
    currentName: String,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Full Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onNameChanged(name.trim())
                    onDismiss()
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TimezoneDialog(
    currentTimezone: String,
    onTimezoneSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timezones = listOf(
        "UTC" to "(GMT+00:00) UTC",
        "Europe/Kyiv" to "(GMT+02:00) Europe/Kyiv",
        "Europe/London" to "(GMT+00:00) Europe/London",
        "America/New_York" to "(GMT-05:00) America/New_York",
        "America/Los_Angeles" to "(GMT-08:00) America/Los_Angeles",
        "Asia/Tokyo" to "(GMT+09:00) Asia/Tokyo",
        "Australia/Sydney" to "(GMT+11:00) Australia/Sydney"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Timezone") },
        text = {
            LazyColumn {
                items(timezones) { (tz, display) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTimezoneSelected(tz)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tz == currentTimezone,
                            onClick = {
                                onTimezoneSelected(tz)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = display,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Keep all existing dialogs (DateFormatDialog, DurationFormatDialog, etc.) but add new ones

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onLogOut: (() -> Unit)? = null
) {
    LaunchedEffect(state.isLoggingOut) {
        if (state.isLoggingOut && state.user == null) {
            onLogOut?.invoke()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    fontWeight = FontWeight.Medium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Error/Success messages
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
                        onClick = { onAction(ProfileAction.ClearError) }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingContent()
        } else {
            ProfileScrollContent(
                state = state,
                onAction = onAction
            )
        }

        // All dialogs
        if (state.showEditNameDialog) {
            EditNameDialog(
                currentName = state.user?.fullName ?: "",
                onNameChanged = { name ->
                    onAction(ProfileAction.UpdateFullName(name))
                },
                onDismiss = { onAction(ProfileAction.HideEditNameDialog) }
            )
        }

        if (state.showTimezoneDialog) {
            TimezoneDialog(
                currentTimezone = state.timezone,
                onTimezoneSelected = { timezone ->
                    onAction(ProfileAction.UpdateTimezone(timezone))
                },
                onDismiss = { onAction(ProfileAction.HideTimezoneDialog) }
            )
        }

        if (state.showLogoutConfirmDialog) {
            AlertDialog(
                onDismissRequest = { onAction(ProfileAction.CancelLogout) },
                icon = {
                    Icon(
                        Lucide.LogOut,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Logout") },
                text = {
                    Text("Are you sure you want to logout? You'll need to sign in again to access your account.")
                },
                confirmButton = {
                    Button(
                        onClick = { onAction(ProfileAction.ConfirmLogout) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(ProfileAction.CancelLogout) }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Keep other existing dialogs...
    }
}