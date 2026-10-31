package br.com.quantosobra.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Salário mínimo BRUTO. Não confundir com salário líquido do empregado. */
data class MinimumWage(
    val cents: Long,
    val effectiveDate: String,
    val source: String,
    val checkedAt: String = ""
) {
    companion object {
        private val brDate = DateTimeFormatter.ofPattern("dd/MM/uuuu")
        /** A API usa R$ com ponto decimal; evita Locale e conversão em centavos incorreta. */
        fun fromSgs(date: String, value: String): MinimumWage? = runCatching {
            val d = LocalDate.parse(date, brDate)
            val cents = BigDecimal(value.trim()).multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP).longValueExact()
            if (cents !in 100L..100_000_000L) return null
            MinimumWage(cents, d.toString(), "BCB")
        }.getOrNull()

        fun latestValid(records: List<MinimumWage>, today: LocalDate): MinimumWage? =
            records.filter { it.cents in 100L..100_000_000L &&
                runCatching { !LocalDate.parse(it.effectiveDate).isAfter(today) }.getOrDefault(false) }
                .maxByOrNull { it.effectiveDate }
    }
}
