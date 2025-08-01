package org.tutvsisvoyi.testkmpapp.data.utils

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object TimeUtils {
    fun durationBetween(startTime: LocalTime, endTime: LocalTime): LocalTime {
        val startTimeSeconds = startTime.toSecondOfDay()
        val endTimeSeconds = endTime.toSecondOfDay()

        val durationSeconds = endTimeSeconds - startTimeSeconds

        val resultHours = durationSeconds / 3600
        val resultMinutes = (durationSeconds % 3600) / 60

        return LocalTime(
            hour = resultHours,
            minute = resultMinutes
        )
    }

    @OptIn(ExperimentalTime::class)
    fun now(): LocalTime {
        val now = Clock.System.now()
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).time
    }
}