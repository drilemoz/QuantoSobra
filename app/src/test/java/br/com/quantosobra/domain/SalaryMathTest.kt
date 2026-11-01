package br.com.quantosobra.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SalaryMathTest {
    @Test fun inss2026ProgressiveAndLimit() {
        assertEquals(0L, SalaryMath.inss2026(0))
        assertEquals(12_158L, SalaryMath.inss2026(162_100))
        assertEquals(24_860L, SalaryMath.inss2026(300_000))
        assertEquals(36_860L, SalaryMath.inss2026(400_000))
        assertEquals(50_151L, SalaryMath.inss2026(500_000))
        assertEquals(64_151L, SalaryMath.inss2026(600_000))
        assertEquals(SalaryMath.inss2026(847_555L), SalaryMath.inss2026(2_000_000L))
    }

    @Test fun irrf2026IncomeBandAndDeduction() {
        assertEquals(0L, SalaryMath.irrf2026(400_000, 36_860))
        assertEquals(0L, SalaryMath.irrf2026(500_000, 50_151))
        assertEquals(38_510L, SalaryMath.irrf2026(600_000, 64_151))
        assertEquals(0L, SalaryMath.irrf2026(0, 0))
        assertEquals(true, SalaryMath.irrf2026(800_000, SalaryMath.inss2026(800_000), 1) <
            SalaryMath.irrf2026(800_000, SalaryMath.inss2026(800_000), 0))
    }

    @Test fun grossPayrollNeverDuplicatesCltBus() {
        val b = Budget(salary = SalaryConfig(SalaryMode.GROSS, grossCents = 300_000),
            transport = TransportConfig(fareCents = 570, ridesPerDay = 2, daysPerMonth = 22,
                mode = TransportMode.CLT))
        val payroll = SalaryMath.breakdown(b)
        assertEquals(18_000L, payroll.transportCents)
        assertEquals(257_140L, payroll.netCents)
        assertEquals(0L, BudgetMath.transportBudgetCost(b))
        assertEquals(payroll.netCents, BudgetMath.availableAfterExpenses(b))
        assertEquals(0L, SalaryMath.breakdown(b.copy(transport = b.transport.copy(mode = TransportMode.FULL))).transportCents)
    }

    @Test fun customBusPayrollOrCashButNotBoth() {
        val b = Budget(salary = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 300_000),
            transport = TransportConfig(500, 2, 22, TransportMode.CUSTOM, companyPercent = 50))
        assertEquals(11_000L, BudgetMath.transportBudgetCost(b))
        assertEquals(0L, SalaryMath.breakdown(b).transportCents)
        val payroll = b.copy(transport = b.transport.copy(customOnPayroll = true))
        assertEquals(0L, BudgetMath.transportBudgetCost(payroll))
        assertEquals(11_000L, SalaryMath.breakdown(payroll).transportCents)
        assertEquals(BudgetMath.availableAfterExpenses(b), BudgetMath.availableAfterExpenses(payroll))
    }

    @Test fun fixedCompanyContributionAndPercentageDeductions() {
        val t = TransportConfig(fareCents = 500, ridesPerDay = 2, daysPerMonth = 22,
            mode = TransportMode.FIXED, companyFixedCents = 8_000)
        assertEquals(14_000L, BudgetMath.transportEmployeeCost(t))
        assertEquals(0L, BudgetMath.transportEmployeeCost(t.copy(companyFixedCents = 25_000)))
        val s = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 300_000,
            autoInss = false, autoIrrf = false, foodBenefitCents = 50_000,
            deductions = listOf(
                PayrollDeduction(1, "Saúde", 0, 500, "salary"),
                PayrollDeduction(2, "VA", 0, 2000, "food")))
        val b = Budget(salary = s, transport = t.copy(customOnPayroll = true))
        assertEquals(15_000L + 10_000L, SalaryMath.breakdown(b).otherCents)
        assertEquals(14_000L, SalaryMath.breakdown(b).transportCents)
        assertEquals(0L, BudgetMath.transportBudgetCost(b))
        assertEquals(261_000L, BudgetMath.salaryNet(b))
        // Valores antes cadastrados continuam entrando no mesmo total, só mudam de categoria.
        val old = Budget(netSalaryCents = 200_000, mobileCents = 3_000, gasCents = 4_000,
            otherBillsCents = 1_000, waterCents = 6_000)
        assertEquals(6_000L, BudgetMath.bills(old))
        assertEquals(8_000L, BudgetMath.extras(old))
        assertEquals(14_000L, BudgetMath.expenses(old))
    }
    @Test fun foodVoucherNotDoubleCountedAndNotCashIncome() {
        val b = Budget(netSalaryCents = 300_000, foodOutsideCents = 50_000,
            salary = SalaryConfig(foodBenefitCents = 20_000))
        assertEquals(300_000L, BudgetMath.salaryNet(b))
        // Sem confirmação de aceitação de VA em restaurantes, não presumir cobertura.
        assertEquals(50_000L, BudgetMath.foodPaidFromCash(b))
        val withAcceptance = b.copy(salary = b.salary.copy(vaForMeals = true))
        assertEquals(30_000L, BudgetMath.foodPaidFromCash(withAcceptance))
        assertEquals(0L, BudgetMath.foodPaidFromCash(withAcceptance.copy(salary =
            withAcceptance.salary.copy(foodBenefitCents = 60_000))))
    }

    @Test fun optionalDiscountsAndLegacyCompatibility() {
        val b = Budget(netSalaryCents = 145_000, transport = TransportConfig(
            fareCents = 500, ridesPerDay = 2, daysPerMonth = 22, mode = TransportMode.CLT,
            basicSalaryCents = 170_000, deductedFromNetSalary = true))
        assertEquals(145_000L, BudgetMath.salaryNet(b))
        assertEquals(0L, BudgetMath.transportBudgetCost(b))
        val gross = b.copy(salary = SalaryConfig(mode = SalaryMode.GROSS, grossCents = 300_000,
            autoInss = false, autoIrrf = false, deductions = listOf(PayrollDeduction(1, "Plano de saúde", 8_000))))
        assertEquals(281_800L, BudgetMath.salaryNet(gross)) // VT 10.200 + plano 8.000 + sem tributos
        assertEquals(0L, BudgetMath.transportBudgetCost(gross))
    }
}
