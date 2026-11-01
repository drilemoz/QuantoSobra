package br.com.quantosobra.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/** Planejamento local: não presume que feriados são automaticamente dias de folga. */
enum class WorkSchedule { MANUAL, WEEKLY, TWELVE_BY_THIRTY_SIX, CUSTOM, FOUR_BY_THREE }

object WorkCalendar {
    fun month(raw: String, fallback: YearMonth = YearMonth.now()): YearMonth = try {
        YearMonth.parse(raw)
    } catch (_: DateTimeParseException) { fallback }

    /** Segunda = bit 0 ... domingo = bit 6. Datas isoladas substituem a regra base.
     * O ciclo 4×3 é ancorado e repetido a cada sete dias, inclusive entre meses.
     */
    fun days(month: YearMonth, mode: WorkSchedule, mask: Int, anchor: LocalDate,
             selected: Set<LocalDate>, extra: Set<LocalDate> = emptySet(),
             off: Set<LocalDate> = emptySet()): Set<LocalDate> {
        if (mode == WorkSchedule.MANUAL) return emptySet()
        return (1..month.lengthOfMonth()).map { month.atDay(it) }.filter { date ->
            val scheduled = when (mode) {
                WorkSchedule.WEEKLY -> mask and (1 shl (date.dayOfWeek.value - 1)) != 0
                WorkSchedule.TWELVE_BY_THIRTY_SIX -> Math.floorMod(ChronoUnit.DAYS.between(anchor, date), 2L) == 0L
                WorkSchedule.FOUR_BY_THREE -> Math.floorMod(ChronoUnit.DAYS.between(anchor, date), 7L) < 4L
                WorkSchedule.CUSTOM -> date in selected
                WorkSchedule.MANUAL -> false
            }
            (scheduled || date in extra) && date !in off
        }.toSet()
    }

    /** Inverte só a data escolhida, sem modificar os demais dias da semana ou o ciclo. */
    fun toggleDate(date: LocalDate, active: Set<LocalDate>, extra: Set<LocalDate>,
                   off: Set<LocalDate>, baseActive: Boolean): Pair<Set<LocalDate>, Set<LocalDate>> =
        if (date in active) (extra - date) to (if (baseActive) off + date else off - date)
        else (if (baseActive) extra - date else extra + date) to (off - date)

    fun parseDates(raw: String): Set<LocalDate> = raw.split(',').take(366).mapNotNull {
        try { LocalDate.parse(it) } catch (_: Exception) { null }
    }.toSet()
    fun encodeDates(dates: Set<LocalDate>): String = dates.sorted().take(366).joinToString(",")
}
