package br.com.quantosobra.data

import android.content.Context
import br.com.quantosobra.domain.MinimumWage
import org.json.JSONArray
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Série oficial 1619/SGS do Banco Central: salário mínimo nominal brasileiro, em R$. */
class MinimumWageRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("wage_reference_v1", Context.MODE_PRIVATE)
    companion object {
        // O BCB limita /ultimos/N a 20 observações; 12 é suficiente para este dado mensal.
        const val ENDPOINT = "https://api.bcb.gov.br/dados/serie/bcdata.sgs.1619/dados/ultimos/12?formato=json"
    }

    fun current(): MinimumWage = MinimumWage(
        cents = prefs.getLong("cents", 162_100L),
        effectiveDate = prefs.getString("effective", "2026-01-01") ?: "2026-01-01",
        source = prefs.getString("source", "Referência oficial embutida • Decreto 12.797/2025")
            ?: "Referência oficial embutida",
        checkedAt = prefs.getString("checked_at", "") ?: ""
    )

    /** Alterar a referência local NÃO altera o salário do usuário, salvo ação separada. */
    fun setManual(cents: Long): MinimumWage {
        require(cents in 100L..100_000_000L)
        val record = MinimumWage(cents, LocalDate.now().toString(), "Valor manual (não verificado)")
        store(record)
        return record
    }

    private fun store(record: MinimumWage) {
        prefs.edit().putLong("cents", record.cents).putString("effective", record.effectiveDate)
            .putString("source", record.source).putString("checked_at", record.checkedAt).apply()
    }

    suspend fun consultOnline(): Result<MinimumWage> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 9000
                conn.readTimeout = 9000
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
                val response = conn.inputStream.bufferedReader().use { it.readText().take(64_000) }
                val data = JSONArray(response)
                val records = (0 until data.length()).mapNotNull { i ->
                    val item = data.optJSONObject(i) ?: return@mapNotNull null
                    MinimumWage.fromSgs(item.optString("data"), item.optString("valor"))
                }
                val latest = MinimumWage.latestValid(records, LocalDate.now())
                    ?: error("Não há referência vigente válida na série 1619")
                val record = latest.copy(source = "Banco Central • SGS 1619", checkedAt = LocalDate.now().toString())
                store(record)
                record
            } finally { conn.disconnect() }
        }
    }
}
