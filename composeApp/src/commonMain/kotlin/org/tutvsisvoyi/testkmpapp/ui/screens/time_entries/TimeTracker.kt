package org.tutvsisvoyi.testkmpapp.ui.screens.time_entries

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ChipColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Workflow
import com.example.compose.backgroundLight
import org.tutvsisvoyi.testkmpapp.domain.model.Project
import org.tutvsisvoyi.testkmpapp.domain.model.TimeEntry

@Composable
fun TimeTrackerContent(
    timeEntry: TimeEntry? = null,
    projects: List<Project>? = null,
    selectedProject: Project? = null,
    onProjectSelected: (Project) -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp, 5.dp)
    ) {
        // Close and Done buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Close Button
            IconButton(
                onClick = {},
                content = {
                    Icon(Icons.Default.Close, "Close")
                },
            )

            // Done Button
            TextButton(
                onClick = {},
                content = {
                    Text("Done")
                }
            )
        }

        // Tracked Time
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "0:00:23",
                fontSize = 20.sp
            )
            Spacer(
                modifier = Modifier
                    .width(10.dp)
            )
            Image(Lucide.Pencil, contentDescription = null)
        }

        // Description
        BasicTextField(
            state = rememberTextFieldState(
                if (timeEntry == null || timeEntry.description == "") "I am working on..."
                else timeEntry.description.toString()
            ),
            lineLimits = TextFieldLineLimits.SingleLine
        )

        // Projects
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier
                .padding(7.dp)
        ) {
            if (selectedProject == null) {
                InputChip(
                    selected = false,
                    onClick = {
                        showDropdownMenu = true
                    },
                    label = { Text("Projects") },
                    leadingIcon = {
                        Icon(Lucide.Workflow, "Projects")
                    },
                )
            }
            else {
                InputChip(
                    selected = false,
                    onClick = {
                        showDropdownMenu = true
                    },
                    label = { Text(selectedProject.name) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = Color(selectedProject.color!!.toColorInt())
                    ),
                )
            }
        }

        // Tags


        DropdownMenu(
            expanded = showDropdownMenu,
            onDismissRequest = {
                showDropdownMenu = false
            }
        ) {
            projects?.forEach { project ->
                DropdownMenuItem(
                    text = {
                        Text(project.name)
                   },
                    onClick = {
                        onProjectSelected(project)
                        showDropdownMenu = false
                    }
                )
            }
        }
    }
}

fun String.toColorInt(): Int {
    if (this[0] == '#') {
        var color = substring(1).toLong(16)
        if (length == 7) {
            color = color or 0x00000000ff000000L
        } else if (length != 9) {
            throw IllegalArgumentException("Unknown color")
        }
        return color.toInt()
    }
    throw IllegalArgumentException("Unknown color")
}