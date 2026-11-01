package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

/** Usado pelo wizard e pelo painel. Não cria gastos recorrentes sem confirmação. */
@Composable
fun RecurringMealsEditor(b: Budget, change: (Budget) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringMeal?>(null) }
    Text("Marmita e refeições recorrentes", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    Hint("Cadastre refeições pagas regularmente. A opção 'Dias de trabalho' usa a escala configurada; não inclua esses mesmos gastos em Lanches extras.")
    OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp)) { Text("＋ Adicionar marmita / gasto recorrente", fontSize = 12.sp) }
    b.recurringMeals.forEach { meal ->
        val occurrences = BudgetMath.mealOccurrences(b, meal)
        HorizontalDivider(color = Lilac)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(meal.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("${Money.format(meal.unitPriceCents)} × $occurrences vez(es) • ${meal.frequency.title}",
                    fontSize = 11.sp, color = Muted)
                Text(Money.format(BudgetMath.mealCost(b, meal)), fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = Purple)
            }
            TextButton(onClick = { editing = meal }) { Text("Editar", fontSize = 12.sp) }
            TextButton(onClick = { change(b.copy(recurringMeals = b.recurringMeals.filterNot { it.id == meal.id })) }) {
                Text("Excluir", color = Negative, fontSize = 12.sp)
            }
        }
    }
    ValueLine("Total de refeições recorrentes", BudgetMath.recurringMealCost(b))
    if (b.recurringMeals.any { it.frequency == MealFrequency.WORK_DAYS }) {
        Hint("Referência: ${BudgetMath.workDays(b)} dias de trabalho em ${WorkCalendar.month(b.transport.workCalendarMonth)}. " +
            "Corrija dias excepcionais na tela de Escala. Mudar de mês altera a projeção mensal.")
    }
    if (showAdd || editing != null) RecurringMealDialog(
        b = b, initial = editing,
        dismiss = { showAdd = false; editing = null },
        save = { newMeal ->
            val meals = if (editing == null) b.recurringMeals + newMeal.copy(
                id = (b.recurringMeals.maxOfOrNull { it.id } ?: 0L) + 1L)
            else b.recurringMeals.map { if (it.id == editing!!.id) newMeal.copy(id = it.id) else it }
            change(b.copy(recurringMeals = meals.take(100)))
            showAdd = false; editing = null
        }
    )
}

@Composable
private fun RecurringMealDialog(b: Budget, initial: RecurringMeal?, dismiss: () -> Unit,
                                save: (RecurringMeal) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "Marmita") }
    var price by remember(initial?.id) {
        mutableStateOf(initial?.unitPriceCents?.takeIf { it > 0L }?.let(Money::input) ?: "")
    }
    var frequency by remember(initial?.id) { mutableStateOf(initial?.frequency ?: MealFrequency.WORK_DAYS) }
    var times by remember(initial?.id) { mutableStateOf((initial?.timesPerPeriod ?: 1).toString()) }
    var expanded by remember { mutableStateOf(false) }
    val cents = Money.parse(price)
    val n = times.toIntOrNull()
    val limit = when (frequency) {
        MealFrequency.WORK_DAYS, MealFrequency.EVERY_DAY -> 20
        MealFrequency.WEEKLY -> 50
        MealFrequency.MONTHLY, MealFrequency.CUSTOM_DAYS -> 366
    }
    val valid = name.isNotBlank() && cents != null && n != null && n in 1..limit
    val temp = RecurringMeal(initial?.id ?: 0L, name.trim().take(80), cents ?: 0L,
        frequency, n ?: 0)
    ModernDialog(if (initial == null) "Adicionar refeição recorrente" else "Editar refeição recorrente",
        dismiss, "Salvar", { if (valid) save(temp) }, confirmEnabled = valid) {
        OutlinedTextField(name, { if (it.length <= 80) name = it }, label = { Text("Nome da refeição") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
        CurrencyTextEntry("Preço por refeição", price, { price = it })
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Frequência: ${frequency.title}  ▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MealFrequency.entries.forEach { item ->
                    DropdownMenuItem(text = { Text(item.title) }, onClick = {
                        frequency = item
                        times = if (item == MealFrequency.CUSTOM_DAYS) "20" else "1"
                        expanded = false
                    })
                }
            }
        }
        OutlinedTextField(times, { if (it.length <= 3 && it.all(Char::isDigit)) times = it },
            label = { Text(when (frequency) {
                MealFrequency.WORK_DAYS -> "Refeições por dia de trabalho"
                MealFrequency.EVERY_DAY -> "Refeições por dia"
                MealFrequency.WEEKLY -> "Vezes por semana"
                MealFrequency.MONTHLY -> "Vezes por mês"
                MealFrequency.CUSTOM_DAYS -> "Dias / vezes informados no mês"
            }) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            isError = n == null || n !in 1..limit,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(15.dp))
        if (valid) {
            Text("Ocorrências no mês: ${BudgetMath.mealOccurrences(b, temp)}", color = Muted)
            ValueLine("Total mensal", BudgetMath.mealCost(b, temp), emphasize = true)
        }
        Hint(if (frequency == MealFrequency.WEEKLY) "Frequência semanal projetada pelo número de dias do mês; pode variar em relação às datas reais."
            else if (frequency == MealFrequency.WORK_DAYS) "A escala de trabalho determina os dias e respeita folgas e plantões extras."
            else "Frequência informada manualmente. Não repita o custo em outros campos.")
    }
}
