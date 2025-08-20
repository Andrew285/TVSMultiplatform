package org.tutvsisvoyi.testkmpapp.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.AtSign
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Send

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailPdfDialog(
    isVisible: Boolean,
    fileName: String?,
    reportType: ReportType,
    startDate: String?,
    endDate: String?,
    isSending: Boolean,
    onDismiss: () -> Unit,
    onSend: (String, String, String) -> Unit
) {
    if (!isVisible) return

    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(generateDefaultSubject(reportType, startDate, endDate)) }
    var body by remember { mutableStateOf(generateDefaultBody(fileName, reportType, startDate, endDate)) }
    var emailError by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
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
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Lucide.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Send PDF Report",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = if (it.isBlank() || !isValidEmail(it)) {
                            "Please enter a valid email address"
                        } else null
                    },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(Lucide.AtSign, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    leadingIcon = {
                        Icon(Lucide.MessageSquare, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message") },
                    leadingIcon = {
                        Icon(Lucide.FileText, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSending
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (isValidEmail(email)) {
                                onSend(email.trim(), subject.trim(), body.trim())
                            }
                        },
                        enabled = !isSending && email.isNotBlank() && isValidEmail(email) && subject.isNotBlank(),
                        modifier = Modifier.widthIn(min = 100.dp)
                    ) {
                        if (isSending) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Sending...")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Lucide.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Send")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateDefaultSubject(reportType: ReportType, startDate: String?, endDate: String?): String {
    return when (reportType) {
        ReportType.DAILY -> "Daily Time Report - ${formatDateShort(startDate ?: "")}"
        ReportType.WEEKLY -> "Weekly Time Report - ${formatDateShort(startDate ?: "")} to ${formatDateShort(endDate ?: "")}"
        ReportType.MONTHLY -> "Monthly Time Report - ${formatDateShort(startDate ?: "")} to ${formatDateShort(endDate ?: "")}"
        ReportType.CUSTOM -> "Time Report - ${formatDateShort(startDate ?: "")} to ${formatDateShort(endDate ?: "")}"
    }
}

private fun generateDefaultBody(fileName: String?, reportType: ReportType, startDate: String?, endDate: String?): String {
    val period = when (reportType) {
        ReportType.DAILY -> "daily period"
        ReportType.WEEKLY -> "weekly period"
        ReportType.MONTHLY -> "monthly period"
        ReportType.CUSTOM -> "custom date range"
    }

    return """Hi,

Please find attached the time tracking report for the $period from ${formatDateShort(startDate ?: "")} to ${formatDateShort(endDate ?: "")}.

The report includes:
• Daily hours breakdown
• Project time distribution
• Summary statistics

Best regards"""
}

private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".") && email.length > 5
}