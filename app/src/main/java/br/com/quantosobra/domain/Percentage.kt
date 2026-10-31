package br.com.quantosobra.domain

import java.math.BigDecimal

/** Percentual em centésimos de ponto; 12,5% equivale a 1250. */
object Percentage {
    fun parseBasisPoints(raw: String): Int? = try {
        val text = raw.trim().replace(',', '.')
        if (!text.matches(Regex("[0-9]+(\\.[0-9]{1,2})?"))) null
        else BigDecimal(text).multiply(BigDecimal(100)).intValueExact().takeIf { it in 0..10_000 }
    } catch (_: Exception) { null }
}
