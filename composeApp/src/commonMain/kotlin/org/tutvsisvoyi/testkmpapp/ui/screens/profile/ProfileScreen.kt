package org.tutvsisvoyi.testkmpapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.compose.koinInject
import org.tutvsisvoyi.testkmpapp.ui.screens.login.LoginScreen

//class ProfileScreen(private val onLogout: () -> Unit) : Screen {
//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        val screenModel: ProfileScreenModel = koinInject()
//        val state by screenModel.state.collectAsState()
//
//        // Handle logout navigation
//        LaunchedEffect(state.isLoggingOut) {
//            if (state.isLoggingOut && state.user == null) {
//                // Navigate to login screen and clear the stack
//                navigator.replaceAll(LoginScreen())
//            }
//        }
//
//        ProfileContent(
//            state = state,
//            onAction = screenModel::handleAction,
//            onBackClick = { navigator.pop() },
//            onLogout = onLogout
//        )
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun ProfileContent(
//    state: ProfileState,
//    onAction: (ProfileAction) -> Unit,
//    onBackClick: () -> Unit,
//    onLogout: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//    ) {
//        // Top App Bar
//        TopAppBar(
//            title = {
//                Text(
//                    "Settings",
//                    fontWeight = FontWeight.Medium
//                )
//            },
//            navigationIcon = {
//                IconButton(onClick = onBackClick) {
////                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                }
//            },
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = MaterialTheme.colorScheme.surface
//            )
//        )
//
//        // Error message
//        if (state.isError) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.errorContainer
//                )
//            ) {
//                Row(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = state.errorMessage ?: "Unknown error",
//                        color = MaterialTheme.colorScheme.onErrorContainer,
//                        modifier = Modifier.weight(1f)
//                    )
//                    TextButton(
//                        onClick = { onAction(ProfileAction.ClearError) }
//                    ) {
//                        Text("Dismiss")
//                    }
//                }
//            }
//        }
//
//        if (state.isLoading) {
//            LoadingContent()
//        } else {
//            ProfileScrollContent(
//                state = state,
//                onAction = onAction
//            )
//        }
//
//        if (state.showLogoutConfirmDialog) {
//            println("DEBUG: Showing logout confirmation dialog")
//            LogoutConfirmationDialog(
//                onConfirm = {
//                    println("DEBUG: Logout confirmed")
//                    onAction(ProfileAction.ConfirmLogout)
//                },
//                onDismiss = {
//                    println("DEBUG: Logout cancelled")
//                    onAction(ProfileAction.CancelLogout)
//                }
//            )
//        }
//    }
//}

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
        ProfileSection(user = state.user)

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
private fun ProfileSection(user: org.tutvsisvoyi.testkmpapp.domain.model.User?) {
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
                ProfileDetailItem(
                    label = "Full name",
                    value = user.fullName.ifEmpty { "Not set" }
                )

                ProfileDetailItem(
                    label = "Email",
                    value = user.email
                )

                ProfileDetailItem(
                    label = "Reports Timezone",
                    value = if (user.timezone.isNotEmpty()) {
                        formatTimezone(user.timezone)
                    } else {
                        "Not set"
                    }
                )

                ProfileDetailItem(
                    label = "Google Sign-In",
                    value = "Enabled" // This would come from API
                )

                ProfileDetailItem(
                    label = "Apple Sign-In",
                    value = "Not Enabled" // This would come from API
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Account Settings",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Tap to edit your details, login methods and your password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                subtitle = dateFormat,
                onClick = { onAction(ProfileAction.ShowDateFormatDialog) }
            )

            SettingsToggleItem(
                title = "Use 24-hour clock",
                checked = use24HourFormat,
                onCheckedChange = { onAction(ProfileAction.UpdateUse24HourFormat(it)) }
            )

            SettingsItem(
                title = "Duration format",
                subtitle = "$durationFormat (0:47:06)",
                onClick = { onAction(ProfileAction.ShowDurationFormatDialog) }
            )

            SettingsItem(
                title = "First day of the week",
                subtitle = firstDayOfWeek,
                onClick = { onAction(ProfileAction.ShowFirstDayDialog) }
            )

            SettingsToggleItem(
                title = "Group similar time entries",
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
    // Add section header for logout
    SectionHeader("ACCOUNT")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Make logout more prominent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isLoggingOut) {
                            onAction(ProfileAction.Logout)
                        }
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    Icon(
//                        imageVector = Icons.Default.ExitToApp,
//                        contentDescription = "Logout",
//                        tint = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.size(24.dp)
//                    )

                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
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
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
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
                    tint = titleColor,
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
        }
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun formatTimezone(timezone: String): String {
    // Format timezone string to be more readable
    // This is a simple example - you might want more sophisticated formatting
    return if (timezone.contains("Europe/Kiev")) {
        "(GMT+03:00) Europe/Kiev"
    } else {
        timezone
    }
}

// Dialog Components

@Composable
private fun DateFormatDialog(
    currentFormat: String,
    onFormatSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormats = listOf(
        "MM/DD/YYYY",
        "DD/MM/YYYY",
        "YYYY-MM-DD",
        "DD.MM.YYYY",
        "MMM DD, YYYY"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Date Format") },
        text = {
            Column {
                dateFormats.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatSelected(format) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = { onFormatSelected(format) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = getDateExample(format),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

@Composable
private fun DurationFormatDialog(
    currentFormat: String,
    onFormatSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val durationFormats = listOf(
        "Improved" to "0:47:06",
        "Classic" to "0.78",
        "Decimal" to "47.1 min"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duration Format") },
        text = {
            Column {
                durationFormats.forEach { (format, example) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFormatSelected(format) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = { onFormatSelected(format) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = example,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

@Composable
private fun FirstDayOfWeekDialog(
    currentDay: String,
    onDaySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val daysOfWeek = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("First Day of Week") },
        text = {
            Column {
                daysOfWeek.forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDaySelected(day) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = day == currentDay,
                            onClick = { onDaySelected(day) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyLarge
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

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
//            Icon(
//                null,
////                Icons.Default.ExitToApp,
//                contentDescription = null,
//                tint = MaterialTheme.colorScheme.error
//            )
        },
        title = {
            Text("Logout")
        },
        text = {
            Text("Are you sure you want to logout? You'll need to sign in again to access your account.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
        // Top App Bar WITHOUT back button
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

        // All the dialogs (same as before)
        if (state.showDateFormatDialog) {
            DateFormatDialog(
                currentFormat = state.dateFormat,
                onFormatSelected = { format ->
                    onAction(ProfileAction.UpdateDateFormat(format))
                },
                onDismiss = { onAction(ProfileAction.HideDateFormatDialog) }
            )
        }

        if (state.showDurationFormatDialog) {
            DurationFormatDialog(
                currentFormat = state.durationFormat,
                onFormatSelected = { format ->
                    onAction(ProfileAction.UpdateDurationFormat(format))
                },
                onDismiss = { onAction(ProfileAction.HideDurationFormatDialog) }
            )
        }

        if (state.showFirstDayDialog) {
            FirstDayOfWeekDialog(
                currentDay = state.firstDayOfWeek,
                onDaySelected = { day ->
                    onAction(ProfileAction.UpdateFirstDayOfWeek(day))
                },
                onDismiss = { onAction(ProfileAction.HideFirstDayDialog) }
            )
        }

        if (state.showLogoutConfirmDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    onAction(ProfileAction.ConfirmLogout)
                    // Navigation handled by LaunchedEffect in ProfileTab
                },
                onDismiss = {
                    onAction(ProfileAction.CancelLogout)
                }
            )
        }
    }
}