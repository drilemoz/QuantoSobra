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
    @Test fun essentialsAddOnlySelectedAndPreserveExisting() {
        val original = listOf(FoodProduct(12, "Arroz", 2_500, 200, "kg"))
        val result = EssentialProducts.add(original, setOf("Arroz", "Ovos", "Leite"))
        assertEquals(3, result.size)
        assertEquals(original[0], result[0])
        assertEquals(setOf("Arroz", "Ovos", "Leite"), result.map { it.name }.toSet())
        assertEquals("dúzia", result.first { it.name == "Ovos" }.unit)
        assertEquals(0L, result.first { it.name == "Leite" }.unitPriceCents)
    }
    @Test fun percentagesUseDecimalWithoutFloatingPoint() {
        assertEquals(1250, Percentage.parseBasisPoints("12,50"))
        assertEquals(1250, Percentage.parseBasisPoints("12.5"))
        assertEquals(10000, Percentage.parseBasisPoints("100"))
        assertEquals(null, Percentage.parseBasisPoints("101"))
        assertEquals(null, Percentage.parseBasisPoints("xx"))
    }
    @Test fun moneyKeepsReaisWhileTyping() {
        assertEquals("9", Money.liveInput("9"))
        assertEquals("90", Money.liveInput("90"))
        assertEquals("900", Money.liveInput("900"))
        assertEquals("1.600", Money.liveInput("1600"))
        assertEquals(90_000L, Money.parse(Money.liveInput("900")))
        assertEquals("900,00", Money.inputGrouped(90_000L))
        assertEquals(5, Money.caretAfterFormatting("12345", 4, "12.345"))
    }
    @Test fun otherWorkScales() {
        val november = YearMonth.of(2026, 11)
        assertEquals(21, WorkCalendar.days(november, WorkSchedule.WEEKLY, 31,
            november.atDay(1), emptySet()).size)
        assertEquals(25, WorkCalendar.days(november, WorkSchedule.WEEKLY, 63,
            november.atDay(1), emptySet()).size)
        assertEquals(17, WorkCalendar.days(november, WorkSchedule.WEEKLY, 15,
            november.atDay(1), emptySet()).size)
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
