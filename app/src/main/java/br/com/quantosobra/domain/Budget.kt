package br.com.quantosobra.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min

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
    val deductedFromNetSalary: Boolean = false
)

data class Budget(
    val netSalaryCents: Long = 0,
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
    fun bills(b: Budget): Long = b.electricityCents + b.waterCents + b.internetCents +
        b.mobileCents + b.gasCents + b.otherBillsCents
    fun extras(b: Budget): Long = b.extras.sumOf { it.amountCents }
    fun transportGross(t: TransportConfig): Long = t.fareCents.coerceAtLeast(0) *
        t.ridesPerDay.coerceIn(0, 20) * t.daysPerMonth.coerceIn(0, 31)

    /** Estimativa: limite geral legal de até 6% do salário básico, ou custo menor. */
    fun transportEmployeeCost(t: TransportConfig): Long {
        val gross = transportGross(t)
        return when (t.mode) {
            TransportMode.NONE -> gross
            TransportMode.FULL -> 0L
            TransportMode.CUSTOM -> (gross * (100 - t.companyPercent.coerceIn(0, 100)) + 50L) / 100L
            TransportMode.CLT -> if (t.basicSalaryCents <= 0) gross
                else min(gross, (t.basicSalaryCents * 6L + 50L) / 100L)
        }
    }

    /** Se já foi deduzido da renda líquida, não contar a mesma despesa de novo. */
    fun transportFromAvailable(t: TransportConfig): Long =
        if (t.deductedFromNetSalary && t.mode != TransportMode.NONE) 0 else transportEmployeeCost(t)

    fun expenses(b: Budget): Long = housing(b) + food(b) + bills(b) +
        extras(b) + transportFromAvailable(b.transport)
    fun availableAfterExpenses(b: Budget): Long = b.netSalaryCents - expenses(b)
    fun availableAfterReserve(b: Budget): Long = availableAfterExpenses(b) - b.reserveCents
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
