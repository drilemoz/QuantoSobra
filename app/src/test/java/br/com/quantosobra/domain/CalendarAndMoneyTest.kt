package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarAndMoneyTest {
    @Test fun liveMoneyTyping() {
        assertEquals("1.600", Money.liveInput("1600"))
        assertEquals("12,00", Money.liveInput("12.00"))
        assertEquals("1.234,56", Money.liveInput("1234,56"))
        assertEquals("1.234,5", Money.liveInput("1234,5"))
        assertEquals("1.234,50", Money.liveInput("1.234,50"))
        assertEquals(123450L, Money.parse(Money.liveInput("1234,50")))
    }
    @Test fun weeklyCalendarOctober() {
        val dates = WorkCalendar.days(YearMonth.of(2026, 10), WorkSchedule.WEEKLY, 31,
            LocalDate.of(2026, 10, 1), emptySet())
        assertEquals(22, dates.size)
        assertEquals(true, LocalDate.of(2026, 10, 30) in dates)
        assertEquals(true, LocalDate.of(2026, 10, 31) !in dates)
    }
    @Test fun alternatingAndCustom() {
        val month = YearMonth.of(2026, 10)
        val shift = WorkCalendar.days(month, WorkSchedule.TWELVE_BY_THIRTY_SIX, 31,
            LocalDate.of(2026, 10, 1), emptySet())
        assertEquals(16, shift.size)
        val one = LocalDate.of(2026, 10, 3)
        val manual = WorkCalendar.days(month, WorkSchedule.CUSTOM, 31,
            LocalDate.now(), WorkCalendar.parseDates(WorkCalendar.encodeDates(setOf(one))))
        assertEquals(setOf(one), manual)
    }
    @Test fun transportWorksWithCalendar() {
        val count = WorkCalendar.days(YearMonth.of(2026, 10), WorkSchedule.WEEKLY, 31,
            LocalDate.of(2026, 10, 1), emptySet()).size
        val t = TransportConfig(fareCents = 570, ridesPerDay = 2,
            daysPerMonth = count, workSchedule = WorkSchedule.WEEKLY, workCalendarMonth = "2026-10")
        assertEquals(25080L, BudgetMath.transportGross(t))
        assertEquals(18000L, BudgetMath.transportEmployeeCost(t.copy(mode = TransportMode.CLT), 300000L))
    }
}
