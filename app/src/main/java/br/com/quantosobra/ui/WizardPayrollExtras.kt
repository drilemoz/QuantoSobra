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
import java.math.BigDecimal

/** Descontos além dos campos de plano de saúde e VA/VR na etapa 3. */
@Composable
fun WizardPayrollExtras(s: SalaryConfig, change: (SalaryConfig) -> Unit) {
    val extras = s.deductions.filterNot { it.id == 900001L || it.id == 900002L }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PayrollDeduction?>(null) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Outros descontos na folha", Modifier.weight(1f), fontWeight = FontWeight.Bold)
        TextButton(onClick = { adding = true }, enabled = s.deductions.size < 100) {
            Text("＋ Adicionar", fontSize = 12.sp)
        }
    }
    if (extras.isEmpty()) Hint("Ex.: empréstimo consignado, adiantamento, contribuição ou faltas.")
    extras.forEach { deduction ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(deduction.name, fontSize = 12.sp)
                Text(if (deduction.percentBasisPoints == null) Money.format(deduction.amountCents)
                    else "${BigDecimal.valueOf(deduction.percentBasisPoints.toLong(), 2).stripTrailingZeros().toPlainString()}% " +
                        "sobre ${if (deduction.percentBase == "food") "VA/VR" else "salário bruto"}",
                    color = Muted, fontSize = 11.sp)
            }
            TextButton(onClick = { editing = deduction }) { Text("Editar", fontSize = 12.sp) }
            TextButton(onClick = { change(s.copy(deductions = s.deductions.filterNot { it.id == deduction.id })) }) {
                Text("Excluir", fontSize = 12.sp, color = Negative)
            }
        }
    }
    if (adding || editing != null) WizardPayrollDeductionDialog(
        initial = editing,
        dismiss = { adding = false; editing = null },
        save = { deduction ->
            val result = if (editing == null) s.deductions + deduction.copy(
                id = maxOf(900004L, (s.deductions.maxOfOrNull { it.id } ?: 0L) + 1L))
            else s.deductions.map { if (it.id == editing!!.id) deduction.copy(id = it.id) else it }
            change(s.copy(deductions = result.take(100)))
            adding = false; editing = null
        }
    )
}

@Composable
private fun WizardPayrollDeductionDialog(initial: PayrollDeduction?, dismiss: () -> Unit,
                                         save: (PayrollDeduction) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var percent by remember(initial?.id) { mutableStateOf(initial?.percentBasisPoints != null) }
    var base by remember(initial?.id) { mutableStateOf(initial?.percentBase ?: "salary") }
    var amount by remember(initial?.id) { mutableStateOf(
        if (initial?.percentBasisPoints != null) BigDecimal.valueOf(initial.percentBasisPoints.toLong(), 2)
            .stripTrailingZeros().toPlainString().replace('.', ',')
        else initial?.amountCents?.let(Money::input) ?: ""
    ) }
    val value = if (percent) Percentage.parseBasisPoints(amount)?.toLong() else Money.parse(amount)
    ModernDialog(if (initial == null) "Adicionar desconto" else "Editar desconto", dismiss, "Salvar",
        onConfirm = {
            if (value != null) save(PayrollDeduction(initial?.id ?: 0L, name.trim().take(80),
                if (percent) 0L else value, if (percent) value.toInt() else null, base))
        }, confirmEnabled = name.isNotBlank() && value != null) {
        OutlinedTextField(name, { if (it.length <= 80) name = it }, label = { Text("Nome do desconto") },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(!percent, onClick = { percent = false; amount = "" }, label = { Text("Valor (R$)") })
            FilterChip(percent, onClick = { percent = true; amount = "" }, label = { Text("Percentual (%)") })
        }
        if (percent) {
            OutlinedTextField(amount, { if (it.length <= 7) amount = it },
                label = { Text("Percentual de 0 a 100") }, suffix = { Text("%") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(base == "salary", { base = "salary" }, label = { Text("Salário bruto") })
                FilterChip(base == "food", { base = "food" }, label = { Text("VA/VR") })
            }
        } else CurrencyTextEntry("Valor descontado por mês", amount, { amount = it })
        Hint("Lance somente o que aparece na folha. O vale-transporte já é calculado na etapa de transporte.")
    }
}
