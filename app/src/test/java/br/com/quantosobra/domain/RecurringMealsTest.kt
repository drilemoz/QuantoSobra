package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RecurringMealsTest {
    private val november = TransportConfig(workSchedule = WorkSchedule.WEEKLY,
        workWeekdaysMask = 31, workCalendarMonth = "2026-11", daysPerMonth = 22)

    @Test fun workDaysUsesScheduleNotAConstant() {
        val b = Budget(transport = november)
        assertEquals(21, BudgetMath.workDays(b))
        assertEquals(21, BudgetMath.mealOccurrences(b, RecurringMeal(1, "Marmita", 1800)))
        assertEquals(37_800L, BudgetMath.recurringMealCost(b.copy(recurringMeals = listOf(
            RecurringMeal(1, "Marmita", 1800)))))
    }

    @Test fun holidayOffAndExtraShiftAffectFoodAndTransportCalendar() {
        val b = Budget(transport = november.copy(workOffDates = "2026-11-02",
            workExtraDates = "2026-11-07"))
        assertEquals(21, BudgetMath.workDays(b))
        assertEquals(22, BudgetMath.workDays(b.copy(transport = b.transport.copy(workOffDates = ""))))
    }

    @Test fun monthlyDailyWeeklyAndCustomOccurrences() {
        val b = Budget(transport = november)
        assertEquals(30, BudgetMath.mealOccurrences(b, RecurringMeal(1, "Café", 400,
            MealFrequency.EVERY_DAY)))
        assertEquals(13, BudgetMath.mealOccurrences(b, RecurringMeal(2, "Lanche", 700,
            MealFrequency.WEEKLY, 3)))
        assertEquals(4, BudgetMath.mealOccurrences(b, RecurringMeal(3, "Pizza", 2000,
            MealFrequency.MONTHLY, 4)))
        assertEquals(18, BudgetMath.mealOccurrences(b, RecurringMeal(4, "Marmita", 2000,
            MealFrequency.CUSTOM_DAYS, 18)))
    }

    @Test fun noDoubleCountAndVoucherSplitNotCashSalary() {
        val b = Budget(netSalaryCents = 150_000, foodEstimateCents = 25_000,
            foodOutsideCents = 8_000, salary = SalaryConfig(foodBenefitCents = 12_000,
                mealVoucherCents = 10_000), transport = november,
            recurringMeals = listOf(RecurringMeal(1, "Marmita", 1800)))
        assertEquals(70_800L, BudgetMath.food(b))
        assertEquals(48_800L, BudgetMath.foodPaidFromCash(b))
        assertEquals(150_000L, BudgetMath.salaryNet(b))
    }

    @Test fun customPayrollDeductionPercentUsesBothVouchers() {
        val s = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 200_000,
            autoInss = false, autoIrrf = false, foodBenefitCents = 15_000,
            mealVoucherCents = 5_000, deductions = listOf(
                PayrollDeduction(8, "Vale", 0, 1000, "food")))
        assertEquals(2_000L, SalaryMath.breakdown(Budget(salary = s)).otherCents)
    }

    @Test fun decimalDraftEditingSupportsIntermediateAndGroupedValues() {
        assertEquals(true, Money.isValidMoneyDraft("2.500,"))
        assertEquals(true, Money.isValidMoneyDraft("2.500,90"))
        assertEquals(250_000L, Money.parse("2.500,"))
        assertEquals(250_090L, Money.parse("2.500,90"))
        assertEquals(false, Money.isValidMoneyDraft("2.5x00"))
    }
}
