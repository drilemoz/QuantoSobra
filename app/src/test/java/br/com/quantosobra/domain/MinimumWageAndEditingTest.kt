package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MinimumWageAndEditingTest {
    @Test fun removingDigitFromThousandDoesNotTurnGroupingIntoDecimal() {
        assertEquals("162", Money.liveInput("1.62", "1.621"))
        assertEquals(16200L, Money.parse(Money.liveInput("1.62", "1.621")))
        assertEquals("946", Money.liveInput("9.46", "9.460"))
        assertEquals(94600L, Money.parse(Money.liveInput("9.46", "9.460")))
    }
    @Test fun userCanStillTypeExplicitDecimals() {
        assertEquals("900", Money.liveInput("900", ""))
        assertEquals("900,50", Money.liveInput("900.50", "900"))
        assertEquals("900,50", Money.liveInput("900,50", "900"))
        assertEquals("1.621", Money.liveInput("1621", "162"))
    }
    @Test fun parserOfBcbUsesCurrencyAndDateCorrectly() {
        val record = MinimumWage.fromSgs("01/01/2026", "1621.00")!!
        assertEquals(162100L, record.cents)
        assertEquals("2026-01-01", record.effectiveDate)
        assertEquals(null, MinimumWage.fromSgs("x", "1621"))
    }
    @Test fun futureWagesAreNotConsideredCurrent() {
        val entries = listOf(MinimumWage(162100,"2026-01-01","BCB"),
            MinimumWage(170000,"2027-01-01","BCB"), MinimumWage(151800,"2025-01-01","BCB"))
        assertEquals(162100L, MinimumWage.latestValid(entries, LocalDate.of(2026,10,31))?.cents)
    }
    @Test fun inssBandsMatchTotalAndUseProgressivity() {
        val tiers = SalaryMath.inssTiers2026(300000L)
        assertEquals(SalaryMath.inss2026(300000L),tiers.sumOf{it.deductionCents})
        assertEquals(162100L,tiers[0].portionCents)
        assertEquals(128184L,tiers[1].portionCents)
        assertEquals(9716L,tiers[2].portionCents)
        assertEquals(0L,tiers[3].portionCents)
    }
    @Test fun minimumIsGrossNotNet() {
        val b = Budget(workType=WorkType.CLT, salary=SalaryConfig(mode=SalaryMode.GROSS,grossCents=162100))
        val d=SalaryMath.breakdown(b)
        assertEquals(162100L,d.grossCents)
        assertEquals(12158L,d.inssCents)
        assertEquals(0L,d.irrfCents)
        assertEquals(149942L,d.netCents)
    }
}
