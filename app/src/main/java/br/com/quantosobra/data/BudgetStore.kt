package br.com.quantosobra.data

import android.content.Context
import br.com.quantosobra.domain.*
import org.json.JSONArray
import org.json.JSONObject

/** Persistência privada, offline, sem servidor e sem permissões de rede. */
class BudgetStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("quanto_sobra_v1", Context.MODE_PRIVATE)

    fun load(): Budget {
        return try {
            val raw = prefs.getString("budget", null) ?: return Budget()
            fromJson(JSONObject(raw))
        } catch (_: Exception) {
            Budget()
        }
    }

    /** Contas existentes não são forçadas a refazer o assistente depois de atualizar. */
    fun onboardingCompleted(): Boolean = prefs.getBoolean("wizard_completed", prefs.contains("budget"))
    fun completeOnboarding() { prefs.edit().putBoolean("wizard_completed", true).commit() }

    /** Um upgrade exibe novidades só uma vez; instalação limpa vai primeiro ao wizard. */
    fun shouldShowWhatsNew(version: String): Boolean =
        StartupFlow.destination(onboardingCompleted(), prefs.getString("last_seen_release", null), version) ==
            StartupDestination.NEWS

    fun markWhatsNewSeen(version: String) {
        prefs.edit().putString("last_seen_release", version).commit()
    }

    /** Limpeza interna equivale a um início novo; não mantém um wizard marcado como concluído. */
    fun clearAndRestartWizard() {
        prefs.edit().remove("budget").putBoolean("wizard_completed", false).commit()
    }

    fun save(b: Budget) {
        prefs.edit().putString("budget", toJson(b).toString()).apply()
    }

    private fun JSONObject.nonNegativeLong(key: String): Long = optLong(key, 0).coerceIn(0, 99_999_999_999L)
    private fun JSONObject.nonNegativeInt(key: String, default: Int, max: Int): Int =
        optInt(key, default).coerceIn(0, max)

    private fun fromJson(j: JSONObject): Budget {
        val food = j.optJSONArray("food") ?: JSONArray()
        val products = (0 until minOf(food.length(), 500)).mapNotNull { index ->
            val p = food.optJSONObject(index) ?: return@mapNotNull null
            val name = p.optString("name").take(80)
            if (name.isBlank()) null else FoodProduct(
                id = p.optLong("id", index.toLong()), name = name,
                unitPriceCents = p.nonNegativeLong("price"),
                quantityHundredths = p.nonNegativeInt("qty", 100, 100_000),
                unit = p.optString("unit", "un").take(32).ifBlank { "un" },
                priceOrigin = if (p.optString("priceOrigin") == "REFERENCIA_ATACADAO") "REFERENCIA_ATACADAO" else "MANUAL"
            )
        }
        val others = j.optJSONArray("extras") ?: JSONArray()
        val extras = (0 until minOf(others.length(), 500)).mapNotNull { index ->
            val e = others.optJSONObject(index) ?: return@mapNotNull null
            val name = e.optString("name").take(80)
            val type = OtherCategory.entries.find { it.name == e.optString("category") }
                ?: OtherCategory.OTHER
            if (name.isBlank()) null else ExtraExpense(
                id = e.optLong("id", index.toLong()), name = name,
                category = type, amountCents = e.nonNegativeLong("value")
            )
        }
        val t = j.optJSONObject("transport") ?: JSONObject()
        val salaryJ = j.optJSONObject("payroll") ?: JSONObject()
        val deductionsJ = salaryJ.optJSONArray("deductions") ?: JSONArray()
        val deductions = (0 until minOf(deductionsJ.length(), 100)).mapNotNull { i ->
            val item = deductionsJ.optJSONObject(i) ?: return@mapNotNull null
            val name = item.optString("name").trim().take(80)
            if (name.isBlank()) null else PayrollDeduction(
                id = item.optLong("id", i.toLong()), name = name,
                amountCents = item.nonNegativeLong("value"),
                percentBasisPoints = item.optInt("percentBP", -1).takeIf { it in 0..10_000 },
                percentBase = if (item.optString("percentBase") == "food") "food" else "salary"
            )
        }
        return Budget(
            workType = WorkType.entries.find { it.name == j.optString("workType") } ?: WorkType.UNKNOWN,
            rentPeriod = RentPeriod.entries.find { it.name == j.optString("rentPeriod") } ?: RentPeriod.MONTHLY,
            iptuPeriod = RentPeriod.entries.find { it.name == j.optString("iptuPeriod") } ?: RentPeriod.MONTHLY,
            netSalaryCents = j.nonNegativeLong("salary"),
            salary = SalaryConfig(
                mode = SalaryMode.entries.find { it.name == salaryJ.optString("mode") } ?: SalaryMode.NET,
                grossCents = salaryJ.nonNegativeLong("gross"),
                autoInss = salaryJ.optBoolean("autoInss", true),
                autoIrrf = salaryJ.optBoolean("autoIrrf", true),
                dependents = salaryJ.nonNegativeInt("dependents", 0, 20),
                foodBenefitCents = salaryJ.nonNegativeLong("foodBenefit"),
                deductions = deductions
            ),
            foodEstimateCents = j.nonNegativeLong("foodEstimate"),
            rentCents = j.nonNegativeLong("rent"),
            condominiumCents = j.nonNegativeLong("condominium"),
            iptuCents = j.nonNegativeLong("iptu"),
            electricityCents = j.nonNegativeLong("electricity"),
            waterCents = j.nonNegativeLong("water"),
            internetCents = j.nonNegativeLong("internet"),
            mobileCents = j.nonNegativeLong("mobile"),
            gasCents = j.nonNegativeLong("gas"),
            otherBillsCents = j.nonNegativeLong("otherBills"),
            foodOutsideCents = j.nonNegativeLong("foodOutside"),
            foodProducts = products,
            transport = TransportConfig(
                commuteMode = CommuteMode.entries.find { it.name == t.optString("commute") }
                    ?: CommuteMode.PUBLIC,
                privateMonthlyCents = t.nonNegativeLong("privateMonthly"),
                fareCents = t.nonNegativeLong("fare"),
                ridesPerDay = t.nonNegativeInt("rides", 2, 20),
                daysPerMonth = t.nonNegativeInt("days", 22, 31),
                mode = TransportMode.entries.find { it.name == t.optString("mode") }
                    ?: TransportMode.NONE,
                basicSalaryCents = t.nonNegativeLong("basicSalary"),
                companyPercent = t.nonNegativeInt("companyPercent", 50, 100),
                companyFixedCents = t.nonNegativeLong("companyFixed"),
                deductedFromNetSalary = t.optBoolean("alreadyDeducted", false),
                customOnPayroll = t.optBoolean("customOnPayroll", false),
                workSchedule = WorkSchedule.entries.find { it.name == t.optString("workSchedule") }
                    ?: WorkSchedule.MANUAL,
                workWeekdaysMask = t.nonNegativeInt("weekdays", 31, 127),
                workCalendarMonth = t.optString("calendarMonth").take(7),
                workAnchorDate = t.optString("anchorDate").take(10),
                customWorkDates = t.optString("customDates").take(5000),
                workExtraDates = t.optString("extraDates").take(5000),
                workOffDates = t.optString("offDates").take(5000)
            ),
            extras = extras,
            reserveCents = j.nonNegativeLong("reserve")
        )
    }

    private fun toJson(b: Budget): JSONObject = JSONObject().apply {
        put("schema", 6)
        put("workType", b.workType.name)
        put("rentPeriod", b.rentPeriod.name)
        put("iptuPeriod", b.iptuPeriod.name)
        put("payroll", JSONObject().apply {
            put("mode", b.salary.mode.name)
            put("gross", b.salary.grossCents)
            put("autoInss", b.salary.autoInss)
            put("autoIrrf", b.salary.autoIrrf)
            put("dependents", b.salary.dependents)
            put("foodBenefit", b.salary.foodBenefitCents)
            put("deductions", JSONArray().apply {
                b.salary.deductions.forEach { d -> put(JSONObject().apply {
                    put("id", d.id); put("name", d.name); put("value", d.amountCents)
                    d.percentBasisPoints?.let { put("percentBP", it) }
                    put("percentBase", d.percentBase)
                }) }
            })
        })
        put("salary", b.netSalaryCents)
        put("foodEstimate", b.foodEstimateCents)
        put("rent", b.rentCents)
        put("condominium", b.condominiumCents)
        put("iptu", b.iptuCents)
        put("electricity", b.electricityCents)
        put("water", b.waterCents)
        put("internet", b.internetCents)
        put("mobile", b.mobileCents)
        put("gas", b.gasCents)
        put("otherBills", b.otherBillsCents)
        put("foodOutside", b.foodOutsideCents)
        put("reserve", b.reserveCents)
        put("food", JSONArray().apply {
            b.foodProducts.forEach { p -> put(JSONObject().apply {
                put("id", p.id); put("name", p.name); put("price", p.unitPriceCents)
                put("qty", p.quantityHundredths); put("unit", p.unit)
                put("priceOrigin", p.priceOrigin)
            }) }
        })
        put("extras", JSONArray().apply {
            b.extras.forEach { e -> put(JSONObject().apply {
                put("id", e.id); put("name", e.name); put("category", e.category.name)
                put("value", e.amountCents)
            }) }
        })
        put("transport", JSONObject().apply {
            put("commute", b.transport.commuteMode.name)
            put("privateMonthly", b.transport.privateMonthlyCents)
            put("fare", b.transport.fareCents)
            put("rides", b.transport.ridesPerDay)
            put("days", b.transport.daysPerMonth)
            put("mode", b.transport.mode.name)
            put("basicSalary", b.transport.basicSalaryCents)
            put("companyPercent", b.transport.companyPercent)
            put("companyFixed", b.transport.companyFixedCents)
            put("alreadyDeducted", b.transport.deductedFromNetSalary)
            put("customOnPayroll", b.transport.customOnPayroll)
            put("workSchedule", b.transport.workSchedule.name)
            put("weekdays", b.transport.workWeekdaysMask)
            put("calendarMonth", b.transport.workCalendarMonth)
            put("anchorDate", b.transport.workAnchorDate)
            put("customDates", b.transport.customWorkDates)
            put("extraDates", b.transport.workExtraDates)
            put("offDates", b.transport.workOffDates)
        })
    }
}
