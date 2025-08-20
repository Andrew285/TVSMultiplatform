package org.tutvsisvoyi.testkmpapp.data.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object CalendarUtils {
    fun firstDayOfThisMonth(): LocalDate {
        val today = today()
        return LocalDate(today.year, today.month, 1)
    }

    fun today(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    /**
     * Converts milliseconds since epoch to LocalDate
     * @param millis milliseconds since January 1, 1970, 00:00:00 GMT
     * @return LocalDate in the system's default timezone
     */
    fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    /**
     * Converts LocalDate to milliseconds since epoch
     * @param localDate the LocalDate to convert
     * @return milliseconds since January 1, 1970, 00:00:00 GMT
     */
    fun localDateToMillis(localDate: LocalDate): Long {
        return localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}

//data class LocalDate(val year: Int, val month: Int, val day: Int) {
//    companion object {
//        fun now() = LocalDate(2024, 1, 15)
//        fun ofEpochDay(epochDay: Long) = LocalDate(2024, 1, 15)
//    }
//    fun format(formatter: Any): String = "Monday, Jan 15, 2024"
//}
//
//data class LocalTime(val hour: Int, val minute: Int) {
//    companion object {
//        fun now() = LocalTime(14, 30)
//        fun of(hour: Int, minute: Int) = LocalTime(hour, minute)
//    }
//    fun format(formatter: Any): String = "$hour:$minute"
//    fun minusHours(hours: Long) = LocalTime((hour - hours).toInt().coerceAtLeast(0), minute)
//}
//
//
//object Duration {
//    fun between(start: LocalTime, end: LocalTime): DurationResult {
//        val startMinutes = start.hour * 60 + start.minute
//        val endMinutes = end.hour * 60 + end.minute
//        val diffMinutes = (endMinutes - startMinutes).coerceAtLeast(0)
//        return DurationResult(diffMinutes / 60, diffMinutes % 60)
//    }
//}
//
//data class DurationResult(val hours: Int, val minutes: Int) {
//    fun toHours(): Long = hours.toLong()
//    fun toMinutes(): Long = (hours * 60 + minutes).toLong()
//}
