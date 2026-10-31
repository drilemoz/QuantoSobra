package br.com.quantosobra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun ExtrasScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    var create by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ExtraExpense?>(null) }
    Page("Outras despesas", "Saúde, lazer, roupas, parcelas e mais", back) {
        SectionTotal("Total mensal de outras despesas", BudgetMath.extras(b))
        SurfaceCard {
            Text("Despesas cadastradas", style = MaterialTheme.typography.titleMedium)
            Hint("Adicione cada gasto mensal. Para móveis comprados em parcelas, registre o valor mensal da parcela.")
            if (b.extras.isEmpty()) Hint("Sua lista está vazia.")
            b.extras.forEach { e ->
                HorizontalDivider(color = Lilac)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).clickable { editing = e }) {
                        Text(e.name, fontWeight = FontWeight.SemiBold)
                        Text(e.category.displayName, fontSize = 12.sp, color = Muted)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Money.format(e.amountCents), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { change(b.copy(extras = b.extras.filterNot { it.id == e.id })) }) {
                            Text("Excluir", fontSize = 12.sp, color = Negative)
                        }
                    }
                }
            }
            AppButton("+ Adicionar despesa") { create = true }
        }
        Hint("Compras únicas e custos de mudança não são despesas fixas. Se pagá-las à vista, registre no mês relevante e remova depois; para planejamento futuro, considere uma reserva mensal.")
    }
    if (create || editing != null) {
        ExtraDialog(initial = editing, dismiss = { create = false; editing = null }) { name, cat, amount ->
            val current = editing
            val updated = if (current == null)
                b.extras + ExtraExpense((b.extras.maxOfOrNull { it.id } ?: 0) + 1, name, cat, amount)
            else b.extras.map { if (it.id == current.id) current.copy(name = name, category = cat, amountCents = amount) else it }
            change(b.copy(extras = updated))
            create = false; editing = null
        }
    }
}

@Composable
private fun ExtraDialog(initial: ExtraExpense?, dismiss: () -> Unit,
                        save: (String, OtherCategory, Long) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: OtherCategory.HEALTH) }
    var amount by remember(initial?.id) {
        mutableStateOf(if ((initial?.amountCents ?: 0L) == 0L) "" else Money.input(initial!!.amountCents))
    }
    var expanded by remember { mutableStateOf(false) }
    val parsed = Money.parse(amount)
    ModernDialog(
        title = if (initial == null) "Nova despesa" else "Editar despesa",
        onDismiss = dismiss, confirmText = "Salvar",
        confirmEnabled = name.isNotBlank() && parsed != null,
        onConfirm = { if (parsed != null) save(name.trim(), category, parsed) }
    ) {
        OutlinedTextField(name, { if (it.length <= 80) name = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Nome da despesa") },
            singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text("${category.displayName}  ▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                OtherCategory.entries.forEach { option ->
                    DropdownMenuItem(text = { Text(option.displayName) }, onClick = {
                        category = option; expanded = false
                    })
                }
            }
        }
        OutlinedTextField(amount, { if (it.length <= 19) amount = it },
            modifier = Modifier.fillMaxWidth(), label = { Text("Valor mensal") },
            prefix = { Text("R$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, isError = parsed == null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp))
        Hint("Para compras parceladas, registre somente a parcela de cada mês.")
    }
}
