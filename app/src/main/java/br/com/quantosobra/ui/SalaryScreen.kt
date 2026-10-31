package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
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
    val addNew = {
        selectedDeduction = null
        deductionName = ""
        deductionValue = ""
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
                Hint("INSS e IRRF seguem tabelas oficiais de 2026, com simplificação para salário mensal. Outros vínculos, pensão alimentícia, rendimentos, férias e 13º podem alterar o valor real.")
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
                            Text(Money.format(d.amountCents), color = Muted, fontSize = 12.sp)
                        }
                        TextButton(onClick = {
                            selectedDeduction = d
                            deductionName = d.name
                            deductionValue = Money.input(d.amountCents)
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
        val parsed = Money.parse(deductionValue)
        ModernDialog(
            title = if (selectedDeduction == null) "Novo desconto" else "Editar desconto",
            onDismiss = { showDeductionEditor = false }, confirmText = "Salvar",
            confirmEnabled = deductionName.trim().isNotEmpty() && parsed != null &&
                (selectedDeduction != null || s.deductions.size < 100),
            onConfirm = {
                val amount = Money.parse(deductionValue)
                val name = deductionName.trim().take(80)
                if (amount != null && name.isNotEmpty()) {
                    val entry = PayrollDeduction(
                        selectedDeduction?.id ?: (s.deductions.maxOfOrNull { it.id } ?: 0L) + 1L,
                        name, amount)
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
            OutlinedTextField(value = deductionValue, onValueChange = { if (it.length <= 19) deductionValue = it },
                modifier = Modifier.fillMaxWidth(), label = { Text("Valor mensal em R$") },
                isError = parsed == null, singleLine = true, shape = RoundedCornerShape(15.dp))
            Hint("Ex.: plano de saúde R$ 80 ou vale-refeição R$ 30. Informe somente a quantia realmente descontada no contracheque.")
        }
    }
}
