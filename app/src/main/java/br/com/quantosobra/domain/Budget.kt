package br.com.quantosobra.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

/** O modo líquido mantém integralmente os dados de versões anteriores. */
enum class SalaryMode { NET, GROSS }

data class PayrollDeduction(
    val id: Long,
    val name: String,
    val amountCents: Long
)

data class SalaryConfig(
    val mode: SalaryMode = SalaryMode.NET,
    val grossCents: Long = 0,
    val autoInss: Boolean = true,
    val autoIrrf: Boolean = true,
    val dependents: Int = 0,
    /** Crédito alimentação que a pessoa pretende efetivamente usar (não é renda em dinheiro). */
    val foodBenefitCents: Long = 0,
    val deductions: List<PayrollDeduction> = emptyList()
)


/** Todos os valores monetários são armazenados em centavos (Long). */
data class FoodProduct(
    val id: Long,
    val name: String,
    val unitPriceCents: Long,
    /** 100 = 1 unidade; 250 = 2,5 unidades. */
    val quantityHundredths: Int
)

enum class OtherCategory(val displayName: String) {
    HEALTH("Saúde"), HYGIENE("Higiene e limpeza"), LEISURE("Lazer"),
    CLOTHING("Roupas"), FURNITURE("Móveis e parcelas"), DEBTS("Dívidas"), OTHER("Outros")
}

data class ExtraExpense(
    val id: Long,
    val name: String,
    val category: OtherCategory,
    val amountCents: Long
)

enum class TransportMode { NONE, CLT, FULL, CUSTOM }

data class TransportConfig(
    val fareCents: Long = 0,
    val ridesPerDay: Int = 2,
    val daysPerMonth: Int = 22,
    val mode: TransportMode = TransportMode.NONE,
    val basicSalaryCents: Long = 0,
    val companyPercent: Int = 50,
    /** Marcar apenas quando o salário líquido informado JÁ descontou o VT. */
    val deductedFromNetSalary: Boolean = false,
    /** Para custeio percentual personalizado: desconto na folha ou pago por fora. */
    val customOnPayroll: Boolean = false
)

data class Budget(
    val netSalaryCents: Long = 0,
    val salary: SalaryConfig = SalaryConfig(),
    val rentCents: Long = 0,
    val condominiumCents: Long = 0,
    val iptuCents: Long = 0,
    val electricityCents: Long = 0,
    val waterCents: Long = 0,
    val internetCents: Long = 0,
    val mobileCents: Long = 0,
    val gasCents: Long = 0,
    val otherBillsCents: Long = 0,
    val foodOutsideCents: Long = 0,
    val foodProducts: List<FoodProduct> = emptyList(),
    val transport: TransportConfig = TransportConfig(),
    val extras: List<ExtraExpense> = emptyList(),
    val reserveCents: Long = 0
)

object BudgetMath {
    fun productCost(item: FoodProduct): Long =
        (item.unitPriceCents.coerceAtLeast(0) * item.quantityHundredths.coerceIn(0, 100_000) + 50L) / 100L

    fun housing(b: Budget): Long = b.rentCents + b.condominiumCents + b.iptuCents
    fun food(b: Budget): Long = b.foodOutsideCents + b.foodProducts.sumOf(::productCost)
    fun foodPaidFromCash(b: Budget): Long = (food(b) - b.salary.foodBenefitCents).coerceAtLeast(0L)
    fun bills(b: Budget): Long = b.electricityCents + b.waterCents + b.internetCents +
        b.mobileCents + b.gasCents + b.otherBillsCents
    fun extras(b: Budget): Long = b.extras.sumOf { it.amountCents }
    fun transportGross(t: TransportConfig): Long = t.fareCents.coerceAtLeast(0) *
        t.ridesPerDay.coerceIn(0, 20) * t.daysPerMonth.coerceIn(0, 31)

    /** Estimativa: limite geral legal de até 6% do salário básico, ou custo menor. */
    fun transportEmployeeCost(t: TransportConfig, salaryBasicFallbackCents: Long = 0L): Long {
        val gross = transportGross(t)
        return when (t.mode) {
            TransportMode.NONE -> gross
            TransportMode.FULL -> 0L
            TransportMode.CUSTOM -> (gross * (100 - t.companyPercent.coerceIn(0, 100)) + 50L) / 100L
            TransportMode.CLT -> {
                val base = t.basicSalaryCents.takeIf { it > 0L } ?: salaryBasicFallbackCents
                if (base <= 0L) gross else min(gross, (base * 6L + 50L) / 100L)
            }
        }
    }

    /** Se já foi deduzido da renda líquida, não contar a mesma despesa de novo. */
    fun transportFromAvailable(t: TransportConfig): Long =
        if (t.deductedFromNetSalary && t.mode != TransportMode.NONE) 0 else transportEmployeeCost(t)

    fun payrollTransport(b: Budget): Long {
        if (b.salary.mode != SalaryMode.GROSS) return 0L
        return when (b.transport.mode) {
            TransportMode.CLT -> transportEmployeeCost(b.transport, b.salary.grossCents)
            TransportMode.CUSTOM -> if (b.transport.customOnPayroll) transportEmployeeCost(b.transport) else 0L
            else -> 0L
        }
    }
    fun salaryNet(b: Budget): Long = if (b.salary.mode == SalaryMode.NET) b.netSalaryCents
        else SalaryMath.breakdown(b).netCents
    fun transportBudgetCost(b: Budget): Long = if (b.salary.mode == SalaryMode.NET)
        transportFromAvailable(b.transport)
    else transportEmployeeCost(b.transport, b.salary.grossCents) - payrollTransport(b)
    fun expenses(b: Budget): Long = housing(b) + foodPaidFromCash(b) + bills(b) +
        extras(b) + transportBudgetCost(b)
    fun availableAfterExpenses(b: Budget): Long = salaryNet(b) - expenses(b)
    fun availableAfterReserve(b: Budget): Long = availableAfterExpenses(b) - b.reserveCents
}

data class SalaryBreakdown(
    val grossCents: Long,
    val inssCents: Long,
    val irrfCents: Long,
    val transportCents: Long,
    val otherCents: Long,
    val netCents: Long
) {
    val totalDeductionsCents: Long get() = inssCents + irrfCents + transportCents + otherCents
}

/** CLT mensal 2026, vínculo único e sem outras bases tributárias; é uma simulação. */
object SalaryMath {
    private fun roundedCents(value: BigDecimal): Long =
        value.setScale(0, RoundingMode.HALF_UP).longValueExact()

    /** INSS progressivo, teto 2026 de R$ 8.475,55. */
    fun inss2026(grossCents: Long): Long {
        val amount = grossCents.coerceAtLeast(0).coerceAtMost(847_555L)
        val limits = longArrayOf(162_100, 290_284, 435_427, 847_555)
        val rates = arrayOf("0.075", "0.09", "0.12", "0.14")
        var previous = 0L
        var sum = BigDecimal.ZERO
        for (i in limits.indices) {
            val portion = (min(amount, limits[i]) - previous).coerceAtLeast(0L)
            sum = sum.add(BigDecimal.valueOf(portion).multiply(BigDecimal(rates[i])))
            previous = limits[i]
        }
        return roundedCents(sum)
    }

    /** IRRF 2026: desconto simplificado ou deduções legais, redução até R$ 7.350. */
    fun irrf2026(grossCents: Long, inssCents: Long, dependents: Int = 0): Long {
        val gross = grossCents.coerceAtLeast(0L)
        val legal = inssCents.coerceAtLeast(0L) + dependents.coerceIn(0, 20) * 18_959L
        val deduction = maxOf(60_720L, legal)
        val base = (gross - deduction).coerceAtLeast(0L)
        val (rate, subtraction) = when {
            base <= 242_880L -> "0" to 0L
            base <= 282_665L -> "0.075" to 18_216L
            base <= 375_105L -> "0.15" to 39_416L
            base <= 466_468L -> "0.225" to 67_549L
            else -> "0.275" to 90_873L
        }
        val initial = (roundedCents(BigDecimal.valueOf(base).multiply(BigDecimal(rate))) - subtraction).coerceAtLeast(0L)
        val reduction = when {
            gross <= 500_000L -> min(initial, 31_289L)
            gross <= 735_000L -> roundedCents(BigDecimal("97862") -
                BigDecimal.valueOf(gross).multiply(BigDecimal("0.133145"))).coerceAtLeast(0L)
            else -> 0L
        }
        return (initial - min(initial, reduction)).coerceAtLeast(0L)
    }

    fun breakdown(b: Budget): SalaryBreakdown {
        val s = b.salary
        if (s.mode == SalaryMode.NET) return SalaryBreakdown(b.netSalaryCents, 0, 0, 0, 0, b.netSalaryCents)
        val gross = s.grossCents.coerceAtLeast(0L)
        val inss = if (s.autoInss) inss2026(gross) else 0L
        val irrf = if (s.autoIrrf) irrf2026(gross, inss, s.dependents) else 0L
        val transit = BudgetMath.payrollTransport(b)
        val other = s.deductions.sumOf { it.amountCents.coerceAtLeast(0L) }
        return SalaryBreakdown(gross, inss, irrf, transit, other,
            gross - inss - irrf - transit - other)
    }
}

object Money {
    private val br = Locale.forLanguageTag("pt-BR")
    fun format(cents: Long): String = NumberFormat.getCurrencyInstance(br).format(cents / 100.0)
    fun input(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString().replace('.', ',')

    /** Aceita 1.234,56, 1234.56, 1234 e 5,70. Valores negativos não são aceitos. */
    fun parse(raw: String): Long? {
        var s = raw.replace("R$", "", ignoreCase = true).replace(" ", "").trim()
        if (s.isEmpty()) return 0
        if (!s.matches(Regex("[0-9.,]+"))) return null
        val comma = s.lastIndexOf(',')
        val dot = s.lastIndexOf('.')
        s = when {
            comma >= 0 && dot >= 0 -> {
                if (comma > dot) s.replace(".", "").replace(',', '.')
                else s.replace(",", "")
            }
            comma >= 0 -> s.replace(".", "").replace(',', '.')
            dot >= 0 && (s.count { it == '.' } > 1 || s.length - dot - 1 == 3) -> s.replace(".", "")
            else -> s
        }
        return try {
            BigDecimal(s).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                .longValueExact().takeIf { it in 0..99_999_999_999L }
        } catch (_: Exception) { null }
    }

    fun quantityHundredths(raw: String): Int? {
        val s = raw.trim().replace(',', '.')
        if (!s.matches(Regex("[0-9]+(\\.[0-9]{0,2})?"))) return null
        return try {
            BigDecimal(s).multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP)
                .intValueExact().takeIf { it in 1..100_000 }
        } catch (_: Exception) { null }
    }
    fun quantityInput(hundredths: Int): String = BigDecimal.valueOf(hundredths.toLong(), 2)
        .stripTrailingZeros().toPlainString().replace('.', ',')
}
