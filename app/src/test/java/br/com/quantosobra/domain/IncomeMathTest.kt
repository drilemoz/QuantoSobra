package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class IncomeMathTest {
    @Test fun meiServicesWithCostsDasAndReserve() {
        val c = IncomeConfig(detailed = true, receiptsCents = 400_000,
            operatingCents = 90_000, businessReserveCents = 30_000,
            meiActivity = MeiActivity.SERVICES, applyMeiDas = true)
        assertEquals(271_395L, IncomeMath.workNet(c, WorkType.MEI))
        assertEquals(8_605L, IncomeMath.dasMei(c, WorkType.MEI))
        assertEquals(0L, IncomeMath.dasMei(c, WorkType.PJ))
    }
    @Test fun overrideDasAndDoNotPresumeTaxes() {
        val c = IncomeConfig(detailed = true, receiptsCents = 100_000,
            meiDasOverrideCents = 9_100, applyMeiDas = true)
        assertEquals(90_900L, IncomeMath.workNet(c, WorkType.MEI))
        assertEquals(100_000L, IncomeMath.workNet(c, WorkType.PJ))
    }
    @Test fun oldIncomeUsesNetWithoutDataLoss() {
        val b = Budget(workType = WorkType.MEI_PJ, netSalaryCents = 333_333)
        assertEquals(333_333L, BudgetMath.salaryNet(b))
        assertEquals(333_333L, b.withWorkType(WorkType.PJ).netSalaryCents)
    }
    @Test fun sourceFrequenciesAndMixedEmployment() {
        val c = IncomeConfig(sources = listOf(
            AdditionalIncome(1,"Pensão",12_000),
            AdditionalIncome(2,"Serviço",1_200,IncomeCadence.WEEKLY),
            AdditionalIncome(3,"Aluguel anual",120_000,IncomeCadence.ANNUAL)))
        assertEquals(27_200L, IncomeMath.sourcesMonthly(c))
        val b = Budget(workType = WorkType.CLT, salary = SalaryConfig(mode = SalaryMode.NET),
            netSalaryCents = 300_000, income = c)
        assertEquals(327_200L, BudgetMath.salaryNet(b))
        assertEquals(300_000L, SalaryMath.breakdown(b).netCents)
    }
    @Test fun switchingCltRemovesOldTransportCalculation() {
        val original = Budget(workType = WorkType.CLT, salary = SalaryConfig(mode = SalaryMode.GROSS,
            grossCents = 250_000), transport = TransportConfig(commuteMode = CommuteMode.PUBLIC,
            mode = TransportMode.CLT, fareCents = 500, ridesPerDay = 2, daysPerMonth = 22,
            deductedFromNetSalary = true))
        val moved = original.withWorkType(WorkType.INFORMAL)
        assertEquals(TransportMode.NONE, moved.transport.mode)
        assertFalse(moved.transport.deductedFromNetSalary)
        assertEquals(SalaryMode.NET, moved.salary.mode)
        assertEquals(22_000L, BudgetMath.transportBudgetCost(moved))
        assertFalse(moved.salary.autoInss)
    }
    @Test fun oldNonCltTransportCLTIsNotDiscounted() {
        val b = Budget(workType = WorkType.OTHER,
            transport = TransportConfig(commuteMode = CommuteMode.PUBLIC,
                mode = TransportMode.CLT, fareCents = 500, ridesPerDay = 2, daysPerMonth = 20,
                deductedFromNetSalary = true))
        assertEquals(20_000L, BudgetMath.transportBudgetCost(b))
        assertEquals(0L, BudgetMath.payrollTransport(b))
    }
    @Test fun publicServantDoesNotUseCltTax() {
        val b = Budget(workType = WorkType.PUBLIC_SERVANT,
            salary = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 600_000),
            income = IncomeConfig(detailed = true, receiptsCents = 600_000,
                taxesCents = 50_000, socialCents = 65_000))
        assertEquals(485_000L, BudgetMath.salaryNet(b))
        assertEquals(0L, SalaryMath.breakdown(b).inssCents)
        assertEquals(0L, SalaryMath.breakdown(b).irrfCents)
    }
    @Test fun withdrawalCannotExceedBusinessCash() {
        val c = IncomeConfig(detailed = true, receiptsCents = 200_000,
            operatingCents = 180_000, personalWithdrawalCents = 100_000)
        assertEquals(20_000L, IncomeMath.workNet(c, WorkType.PJ))
        assertEquals(-10_000L, IncomeMath.workNet(c.copy(operatingCents = 210_000), WorkType.PJ))
    }
    @Test fun meiAnnualProportionalAndWarningData() {
        val c = IncomeConfig(meiOpeningMonth = 7, receiptsCents = 100_000,
            meiPreviousRevenueCents = 2_800_000)
        assertEquals(4_050_000L, IncomeMath.meiCap2026(c))
        assertEquals(2_900_000L, IncomeMath.meiRunningTotal(c))
    }
    @Test fun historyOnlyUsesProvidedObservations() {
        assertNull(IncomeMath.averageHistory(IncomeConfig()))
        val c = IncomeConfig(historyCents = listOf(120_000, 80_000, 100_000))
        assertEquals(100_000L, IncomeMath.averageHistory(c))
        assertEquals(80_000L, IncomeMath.conservativeHistory(c))
    }
    @Test fun foodDiscountOnlyOnceOnNonClt() {
        val s = FoodBenefitMath.change(SalaryConfig(foodBenefitCents = 50_000,
            foodDeductionAlreadyInNet = false), FoodDiscountMethod.FIXED, fixedCents = 5_000)
        val b = Budget(workType = WorkType.SELF_EMPLOYED,
            netSalaryCents = 150_000, salary = s)
        assertEquals(145_000L, BudgetMath.salaryNet(b))
        assertEquals(150_000L, BudgetMath.salaryNet(b.copy(salary = s.copy(foodDeductionAlreadyInNet = true))))
    }
    @Test fun dailyIncomeUsesOnlyPaidDays() {
        val c = IncomeConfig(detailed = true, receiptsCents = 0, dailyBased = true,
            dailyRateCents = 12_500, workedDays = 18, operatingCents = 40_000)
        assertEquals(225_000L, IncomeMath.revenue(c, WorkType.SELF_EMPLOYED))
        assertEquals(185_000L, IncomeMath.workNet(c, WorkType.SELF_EMPLOYED))
        assertEquals(0L, IncomeMath.revenue(c, WorkType.PJ))
    }
    @Test fun publicAndPjTypesAreManualOnly() {
        val state = IncomeConfig(detailed = true, receiptsCents = 300_000,
            publicAdministration = PublicAdministration.STATE, publicPension = PublicPension.RPPS,
            pjTaxRegime = PjTaxRegime.SIMPLES)
        assertEquals(300_000L, IncomeMath.workNet(state, WorkType.PUBLIC_SERVANT))
        assertEquals(300_000L, IncomeMath.workNet(state, WorkType.PJ))
    }
}
