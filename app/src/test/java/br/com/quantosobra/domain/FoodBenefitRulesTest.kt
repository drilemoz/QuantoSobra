package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FoodBenefitRulesTest {
    @Test fun noPresumedDiscountWhenUnknown() {
        val s = SalaryConfig(foodBenefitCents = 40_000, mealVoucherCents = 30_000)
        assertEquals(FoodDiscountMethod.UNKNOWN, FoodBenefitMath.method(s))
        assertEquals(0L, FoodBenefitMath.deductionCents(s))
        assertEquals(150_000L, BudgetMath.salaryNet(Budget(netSalaryCents = 150_000, salary = s)))
    }

    @Test fun percentIsOfBenefitsNotSalaryAndNoDoubleDeduction() {
        val base = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 200_000,
            autoInss = false, autoIrrf = false,
            foodBenefitCents = 40_000, mealVoucherCents = 30_000)
        val s = FoodBenefitMath.change(base, FoodDiscountMethod.PERCENT, percentageBP = 1_000)
        val b = Budget(salary = s)
        assertEquals(7_000L, FoodBenefitMath.deductionCents(s))
        assertEquals(193_000L, SalaryMath.breakdown(b).netCents)
        assertEquals(7_000L, SalaryMath.breakdown(b).otherCents)
        assertEquals(1, s.deductions.count { it.id == FoodBenefitMath.DEDUCTION_ID })
        val updated = FoodBenefitMath.change(s, FoodDiscountMethod.PERCENT, percentageBP = 2_000)
        assertEquals(14_000L, FoodBenefitMath.deductionCents(updated))
        assertEquals(1, updated.deductions.count { it.id == FoodBenefitMath.DEDUCTION_ID })
    }

    @Test fun netSalaryAlreadyIncludesDeductionByDefault() {
        val s = FoodBenefitMath.change(SalaryConfig(foodBenefitCents = 30_000),
            FoodDiscountMethod.FIXED, fixedCents = 4_000)
        assertEquals(145_000L, BudgetMath.salaryNet(Budget(netSalaryCents = 145_000, salary = s)))
        assertEquals(141_000L, BudgetMath.salaryNet(Budget(netSalaryCents = 145_000,
            salary = s.copy(foodDeductionAlreadyInNet = false))))
        assertEquals(4_000L, SalaryMath.breakdown(Budget(netSalaryCents = 145_000,
            salary = s.copy(foodDeductionAlreadyInNet = false))).otherCents)
    }

    @Test fun oldFixedDiscountIsRecoveredAndCanBeDisabled() {
        val legacy = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 200_000,
            autoInss = false, autoIrrf = false,
            deductions = listOf(PayrollDeduction(900001, "Plano", 3_000),
                PayrollDeduction(900002, "VA/VR", 5_000)))
        assertEquals(FoodDiscountMethod.FIXED, FoodBenefitMath.method(legacy))
        assertEquals(5_000L, FoodBenefitMath.deductionCents(legacy))
        val none = FoodBenefitMath.change(legacy, FoodDiscountMethod.NONE)
        assertEquals(1, none.deductions.size)
        assertEquals(3_000L, SalaryMath.breakdown(Budget(salary = none)).otherCents)
        assertEquals(FoodDiscountMethod.NONE, FoodBenefitMath.method(none))
    }

    @Test fun alertReferenceIsNotAutomaticRejection() {
        val s = SalaryConfig(foodBenefitCents = 40_000, mealVoucherCents = 30_000)
        val over = FoodBenefitMath.change(s, FoodDiscountMethod.FIXED, fixedCents = 15_000)
        assertEquals(true, FoodBenefitMath.exceedsPatReference(over))
        assertEquals(15_000L, FoodBenefitMath.deductionCents(over))
        assertEquals(false, FoodBenefitMath.exceedsPatReference(
            FoodBenefitMath.change(s, FoodDiscountMethod.PERCENT, percentageBP = 2_000)))
    }

    @Test fun vaAndVrHaveConservativeSeparateEligibility() {
        val b = Budget(foodEstimateCents = 50_000, foodOutsideCents = 10_000,
            salary = SalaryConfig(foodBenefitCents = 40_000, mealVoucherCents = 30_000))
        val c = FoodBenefitMath.coverage(b)
        assertEquals(40_000L, c.vaMarketCents)
        assertEquals(10_000L, c.vrMealsCents)
        assertEquals(50_000L, c.totalUsedCents)
        assertEquals(10_000L, BudgetMath.foodPaidFromCash(b))
    }

    @Test fun vrCannotCoverMarketWithoutPermission() {
        val b = Budget(foodEstimateCents = 50_000,
            salary = SalaryConfig(mealVoucherCents = 40_000))
        assertEquals(0L, FoodBenefitMath.coverage(b).totalUsedCents)
        assertEquals(50_000L, BudgetMath.foodPaidFromCash(b))
        assertEquals(10_000L, BudgetMath.foodPaidFromCash(
            b.copy(salary = b.salary.copy(vrForMarket = true))))
    }

    @Test fun allocationNeverSpendsMoreThanGoodsOrCredit() {
        val b = Budget(foodEstimateCents = 30_000, foodOutsideCents = 10_000,
            salary = SalaryConfig(foodBenefitCents = 50_000, mealVoucherCents = 40_000,
                vaForMarket = true, vaForMeals = true, vrForMarket = true, vrForMeals = true))
        val c = FoodBenefitMath.coverage(b)
        assertEquals(40_000L, c.totalUsedCents)
        assertEquals(0L, BudgetMath.foodPaidFromCash(b))
        assertEquals(true, c.vaUsedCents <= b.salary.foodBenefitCents)
        assertEquals(true, c.vrUsedCents <= b.salary.mealVoucherCents)
    }

    @Test fun optOutRemovesCardCoverageWithoutHidingCredit() {
        val b = Budget(foodEstimateCents = 20_000, foodOutsideCents = 20_000,
            salary = SalaryConfig(foodBenefitCents = 30_000, mealVoucherCents = 30_000,
                vaForMarket = false, vrForMeals = false))
        assertEquals(0L, FoodBenefitMath.coverage(b).totalUsedCents)
        assertEquals(40_000L, BudgetMath.foodPaidFromCash(b))
        assertEquals(60_000L, b.salary.totalFoodBenefitCents)
    }
}
