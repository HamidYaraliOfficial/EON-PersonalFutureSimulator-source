package com.eon.futuresimulator.core.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Small, dependency-free date/time helpers shared by every engine and screen. */
object DateTimeUtils {

    fun nowEpochMillis(): Long = System.currentTimeMillis()

    fun todayLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        LocalDate.now(zone)

    fun epochMillisToLocalDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun localDateToEpochMillis(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun daysBetween(start: LocalDate, end: LocalDate): Long =
        java.time.temporal.ChronoUnit.DAYS.between(start, end)

    fun isWeekend(date: LocalDate): Boolean =
        date.dayOfWeek == DayOfWeek.FRIDAY || date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    /** Human-ish "in 3 days" / "12h 40m" style duration label, locale-agnostic (caller supplies unit strings). */
    fun formatMinutes(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    fun zonedNow(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime = ZonedDateTime.now(zone)
}
