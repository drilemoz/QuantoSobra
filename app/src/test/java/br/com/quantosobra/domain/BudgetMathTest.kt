package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetMathTest {
    @Test fun baseline() {
        val b = Budget(netSalaryCents = 300_000, rentCents = 135_000,
            electricityCents = 8_000, waterCents = 6_000,
            internetCents = 10_000, mobileCents = 3_000,
            foodOutsideCents = 80_000)
        assertEquals(242_000, BudgetMath.expenses(b))
        assertEquals(58_000, BudgetMath.availableAfterExpenses(b))
    }

    @Test fun transportCltAndCompany() {
        val t = TransportConfig(fareCents = 570, ridesPerDay = 2, daysPerMonth = 22,
            mode = TransportMode.CLT, basicSalaryCents = 300_000)
        assertEquals(25_080, BudgetMath.transportGross(t))
        assertEquals(18_000, BudgetMath.transportEmployeeCost(t))
        assertEquals(0, BudgetMath.transportFromAvailable(t.copy(deductedFromNetSalary = true)))
        assertEquals(0, BudgetMath.transportEmployeeCost(t.copy(mode = TransportMode.FULL)))
        assertEquals(12_540, BudgetMath.transportEmployeeCost(t.copy(mode = TransportMode.CUSTOM, companyPercent = 50)))
        assertEquals(25_080, BudgetMath.transportEmployeeCost(t.copy(mode = TransportMode.NONE)))
    }

    @Test fun cheapTransportationCannotExceedActualFare() {
        val t = TransportConfig(100, 2, 1, TransportMode.CLT, 300_000)
        assertEquals(200, BudgetMath.transportEmployeeCost(t))
    }

    @Test fun foodWithFractionalQuantity() {
        val p = FoodProduct(1, "Arroz", 799, 250)
        assertEquals(1_998, BudgetMath.productCost(p))
        assertEquals(1_998, BudgetMath.food(Budget(foodProducts = listOf(p))))
    }

    @Test fun moneyLocalized() {
        assertEquals(135_000L, Money.parse("1.350,00"))
        assertEquals(570L, Money.parse("5,70"))
        assertEquals(570L, Money.parse("5.70"))
        assertEquals(135_000L, Money.parse("1350"))
        assertEquals(135_000L, Money.parse("R$ 1.350,00"))
        assertEquals(0L, Money.parse(""))
        assertNull(Money.parse("1,2,3"))
        assertNull(Money.parse("-100"))
        assertEquals(250, Money.quantityHundredths("2,5"))
    }

    @Test fun negativeBalanceAndReserve() {
        val b = Budget(netSalaryCents = 162_100, rentCents = 80_000,
            foodOutsideCents = 50_000, internetCents = 10_000, mobileCents = 3_000,
            waterCents = 6_000, electricityCents = 8_000, gasCents = 4_000,
            reserveCents = 10_000)
        assertEquals(1_100, BudgetMath.availableAfterExpenses(b))
        assertEquals(-8_900, BudgetMath.availableAfterReserve(b))
    }
}
