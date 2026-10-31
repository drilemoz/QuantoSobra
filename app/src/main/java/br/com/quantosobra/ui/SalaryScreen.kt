package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

/** Central de contracheque: oferece modo simples e modo bruto sem alterar os dados antigos. */
@Composable
fun SalaryScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    val s = b.salary
    val isGross = s.mode == SalaryMode.GROSS
    val summary = SalaryMath.breakdown(b)
    val update: (SalaryConfig) -> Unit = { change(b.copy(salary = it)) }
    var selectedDeduction by remember { mutableStateOf<PayrollDeduction?>(null) }
    var showDeductionEditor by remember { mutableStateOf(false) }
    var deductionName by remember { mutableStateOf("") }
    var deductionValue by remember { mutableStateOf("") }
    var deductionPercentMode by remember { mutableStateOf(false) }
    var deductionBase by remember { mutableStateOf("salary") }
    var showRules by remember { mutableStateOf(false) }
    val addNew = {
        selectedDeduction = null
        deductionName = ""
        deductionValue = ""
        deductionPercentMode = false
        deductionBase = "salary"
        showDeductionEditor = true
    }

    Page("Meu salário", "Do bruto ao que realmente entra na conta", back) {
        SectionTotal(if (isGross) "Salário líquido estimado" else "Salário líquido informado", BudgetMath.salaryNet(b))
        SurfaceCard {
            Text("Como quer informar seu salário?", color = Ink, fontSize = 15.sp,
                fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { update(s.copy(mode = SalaryMode.NET)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isGross) Purple else Lilac,
                        contentColor = if (!isGross) Color.White else DeepPurple)
                ) { Text("Já sei o líquido", fontSize = 12.sp, maxLines = 1) }
                Button(
                    onClick = { update(s.copy(mode = SalaryMode.GROSS)) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isGross) Purple else Lilac,
                        contentColor = if (isGross) Color.White else DeepPurple)
                ) { Text("Calcular do bruto", fontSize = 12.sp, maxLines = 1) }
            }
            if (!isGross) {
                FieldAmount("Salário líquido que recebo", b.netSalaryCents,
                    { change(b.copy(netSalaryCents = it)) })
                Hint("Use o valor que cai na conta depois dos descontos. O app conserva seus dados anteriores e evita descontar o transporte já pago na folha.")
            } else {
                FieldAmount("Salário bruto mensal", s.grossCents, { update(s.copy(grossCents = it)) })
                Hint("Para empregado CLT com um único vínculo e salário mensal fixo. Esta é uma estimativa, não substitui o holerite.")
                HorizontalDivider(color = Lilac)
                MinimumWageBox(applyToSalary = { update(s.copy(grossCents = it)) })
                Hint("O mínimo é uma referência BRUTA, não o valor líquido depois do INSS. Só muda seu salário se você tocar em Usar.")
            }
        }
        if (isGross) {
            SurfaceCard {
                Text("Descontos calculados", color = Ink, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("INSS progressivo • 2026", color = Ink, fontWeight = FontWeight.Medium)
                        Text(Money.format(summary.inssCents), color = Muted, fontSize = 12.sp)
                    }
                    Switch(checked = s.autoInss, onCheckedChange = { update(s.copy(autoInss = it)) })
                }
                HorizontalDivider(color = Lilac)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Imposto de renda • 2026", color = Ink, fontWeight = FontWeight.Medium)
                        Text(Money.format(summary.irrfCents), color = Muted, fontSize = 12.sp)
                    }
                    Switch(checked = s.autoIrrf, onCheckedChange = { update(s.copy(autoIrrf = it)) })
                }
                if (s.autoIrrf) FieldCount("Dependentes para IRRF", s.dependents, 0..20,
                    { update(s.copy(dependents = it)) })
                Hint("INSS é progressivo: 7,5%, 9%, 12% e 14% apenas sobre cada faixa, não sobre todo o salário. IRRF depende da base tributável e de deduções.")
                TextButton(onClick = { showRules = true }) { Text("ⓘ Ver faixas, alíquotas e valores descontados", color = Purple) }
            }
            SurfaceCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Outros descontos na folha", color = Ink, fontWeight = FontWeight.Bold)
                        Text("Só os que aparecem no seu contracheque", fontSize = 11.sp, color = Muted)
                    }
                    FilledTonalButton(onClick = addNew, colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Lilac, contentColor = Purple)) { Text("+ Adicionar") }
                }
                if (s.deductions.isEmpty()) {
                    Hint("Plano de saúde, vale-refeição, adiantamento salarial, contribuição autorizada, faltas ou outros descontos. Não cadastre o VT aqui: ele já vem de Transporte.")
                }
                s.deductions.forEach { d ->
                    HorizontalDivider(color = Lilac)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(d.name, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            val rate = d.percentBasisPoints
                            Text(if (rate == null) Money.format(d.amountCents)
                                else "${rate / 100.0}% de ${if (d.percentBase == "food") "VA/VR" else "salário bruto"}",
                                color = Muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = {
                            selectedDeduction = d
                            deductionName = d.name
                            deductionPercentMode = d.percentBasisPoints != null
                            deductionBase = d.percentBase
                            deductionValue = if (d.percentBasisPoints != null)
                                java.math.BigDecimal.valueOf(d.percentBasisPoints.toLong(), 2).stripTrailingZeros()
                                    .toPlainString().replace('.', ',') else Money.input(d.amountCents)
                            showDeductionEditor = true
                        }) { Text("Editar") }
                        TextButton(onClick = {
                            update(s.copy(deductions = s.deductions.filterNot { it.id == d.id }))
                        }) { Text("×", color = Negative, fontSize = 20.sp) }
                    }
                }
            }
            SurfaceCard {
                Text("Seu contracheque resumido", color = Ink, fontWeight = FontWeight.Bold)
                ValueLine("Salário bruto", summary.grossCents)
                ValueLine("INSS", summary.inssCents)
                ValueLine("IRRF", summary.irrfCents)
                ValueLine("Vale-transporte na folha", summary.transportCents)
                ValueLine("Demais descontos", summary.otherCents)
                HorizontalDivider(color = Lilac)
                ValueLine("Total dos descontos", summary.totalDeductionsCents)
                ValueLine("Líquido para o orçamento", summary.netCents, emphasize = true)
                if (summary.netCents < 0L) Text("Atenção: descontos maiores que o salário. Revise os valores.", color = Negative)
                Hint("O vale-transporte CLT é calculado em Transporte e aparece aqui automaticamente. Se a empresa oferecer VA/VR como benefício, só informe aqui o desconto cobrado na folha, não o valor do benefício recebido.")
            }
        }
        SurfaceCard {
            Text("Benefício de alimentação", color = Ink, fontWeight = FontWeight.Bold)
            FieldAmount("Vale-alimentação/refeição que vou usar", s.foodBenefitCents,
                { update(s.copy(foodBenefitCents = it)) })
            Hint("Valor estimado utilizado no mês. Entra como abatimento da categoria Alimentação, sem aumentar o salário em dinheiro. Não confunda com o valor descontado do holerite.")
        }
    }

    if (showDeductionEditor && isGross) {
        val parsed = if (deductionPercentMode) Percentage.parseBasisPoints(deductionValue)?.toLong()
            else Money.parse(deductionValue)
        ModernDialog(
            title = if (selectedDeduction == null) "Novo desconto" else "Editar desconto",
            onDismiss = { showDeductionEditor = false }, confirmText = "Salvar",
            confirmEnabled = deductionName.trim().isNotEmpty() && parsed != null &&
                (selectedDeduction != null || s.deductions.size < 100),
            onConfirm = {
                val amount = if (deductionPercentMode) Percentage.parseBasisPoints(deductionValue)?.toLong()
                    else Money.parse(deductionValue)
                val name = deductionName.trim().take(80)
                if (amount != null && name.isNotEmpty()) {
                    val entry = PayrollDeduction(
                        selectedDeduction?.id ?: (s.deductions.maxOfOrNull { it.id } ?: 0L) + 1L,
                        name, if (deductionPercentMode) 0L else amount,
                        if (deductionPercentMode) amount.toInt() else null, deductionBase)
                    update(s.copy(deductions = if (selectedDeduction == null)
                        (s.deductions + entry).take(100)
                    else s.deductions.map { if (it.id == entry.id) entry else it }))
                    showDeductionEditor = false
                }
            }
        ) {
            OutlinedTextField(value = deductionName, onValueChange = { if (it.length <= 80) deductionName = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Nome do desconto") }, singleLine = true,
                shape = RoundedCornerShape(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !deductionPercentMode, onClick = { deductionPercentMode = false; deductionValue = "" },
                    label = { Text("Valor (R$)") })
                FilterChip(selected = deductionPercentMode, onClick = { deductionPercentMode = true; deductionValue = "" },
                    label = { Text("Percentual (%)") })
            }
            if (deductionPercentMode) {
                OutlinedTextField(deductionValue, { if (it.length <= 7) deductionValue = it },
                    label = { Text("Percentual, de 0 a 100") }, suffix = { Text("%") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(15.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = deductionBase == "salary", onClick = { deductionBase = "salary" },
                        label = { Text("Do salário bruto") })
                    FilterChip(selected = deductionBase == "food", onClick = { deductionBase = "food" },
                        label = { Text("Do VA/VR") })
                }
                Hint("O percentual informado será recalculado quando a base mudar. É uma configuração manual, não um desconto previsto para todas as empresas.")
            } else CurrencyTextEntry("Valor mensal em R$", deductionValue, { deductionValue = it })
            Hint("Informe apenas o que sua empresa realmente desconta. O vale-transporte já vem da tela Transporte.")
        }
    }
    if (showRules) PayrollRulesDialog(b) { showRules = false }
}

