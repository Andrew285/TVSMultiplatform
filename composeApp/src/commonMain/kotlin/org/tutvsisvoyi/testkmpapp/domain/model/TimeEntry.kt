package org.tutvsisvoyi.testkmpapp.domain.model

import kotlinx.datetime.Instant

data class TimeEntry(
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
    val isRunning: Boolean get() = stop == null && duration < 0

    fun getFormattedDuration(): String {
        val totalSeconds = if (isRunning) {
            (kotlinx.datetime.Clock.System.now() - start).inWholeSeconds
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
