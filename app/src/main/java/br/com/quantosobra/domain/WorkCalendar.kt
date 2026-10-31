package br.com.quantosobra.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/** Planejamento local: não presume que feriados são automaticamente dias de folga. */
enum class WorkSchedule { MANUAL, WEEKLY, TWELVE_BY_THIRTY_SIX, CUSTOM }

object WorkCalendar {
    fun month(raw: String, fallback: YearMonth = YearMonth.now()): YearMonth = try {
        YearMonth.parse(raw)
    } catch (_: DateTimeParseException) { fallback }

    /** Segunda = bit 0 ... domingo = bit 6. */
    fun days(month: YearMonth, mode: WorkSchedule, mask: Int, anchor: LocalDate,
             selected: Set<LocalDate>): Set<LocalDate> {
        if (mode == WorkSchedule.MANUAL) return emptySet()
        return (1..month.lengthOfMonth()).map { month.atDay(it) }.filter { date ->
            when (mode) {
                WorkSchedule.WEEKLY -> mask and (1 shl (date.dayOfWeek.value - 1)) != 0
                WorkSchedule.TWELVE_BY_THIRTY_SIX -> ChronoUnit.DAYS.between(anchor, date) % 2L == 0L
                WorkSchedule.CUSTOM -> date in selected
                WorkSchedule.MANUAL -> false
            }
        }.toSet()
    }

    fun parseDates(raw: String): Set<LocalDate> = raw.split(',').take(366).mapNotNull {
        try { LocalDate.parse(it) } catch (_: Exception) { null }
    }.toSet()
    fun encodeDates(dates: Set<LocalDate>): String = dates.sorted().take(366).joinToString(",")
}
