package org.tutvsisvoyi.testkmpapp.domain.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.ExperimentalTime

data class TimeEntry @OptIn(ExperimentalTime::class) constructor(
    val id: Long? = null,
    val description: String? = null,
    val projectId: Long? = null,
    val workspaceId: Long,
    val start: Instant,
    val stop: Instant? = null,
    val duration: Long, // Duration in seconds
    val tags: List<String> = emptyList(),
    val project: Project? = null
) {
    @OptIn(ExperimentalTime::class)
    val isRunning: Boolean get() = stop == null && duration < 0

    @OptIn(ExperimentalTime::class)
    fun getFormattedDuration(): String {
        val totalSeconds = if (isRunning) {
            (Clock.System.now() - start).inWholeSeconds
        } else {
            duration
        }

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "$hours:$minutes:$seconds}"
            else -> "$minutes:$seconds}"
        }
    }
}
