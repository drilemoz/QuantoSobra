package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.quantosobra.domain.*

@Composable
fun TransportScreen(b: Budget, change: (Budget) -> Unit, back: () -> Unit) {
    val t = b.transport
    val update: (TransportConfig) -> Unit = { change(b.copy(transport = it)) }
    val gross = BudgetMath.transportGross(t)
    val employee = BudgetMath.transportEmployeeCost(t, if (b.salary.mode == SalaryMode.GROSS) b.salary.grossCents else 0L)
    val company = gross - employee

    Page("Transporte", "Passagens e benefícios da empresa", back) {
        SectionTotal("Descontado do orçamento", BudgetMath.transportBudgetCost(b))
        SurfaceCard {
            Text("Como você vai ao trabalho?", style = MaterialTheme.typography.titleMedium)
            listOf(
                CommuteMode.PUBLIC to "Transporte público",
                CommuteMode.PRIVATE to "Carro ou moto",
                CommuteMode.BIKE_WALK to "Bicicleta ou a pé",
                CommuteMode.HOME to "Trabalho remoto"
            ).forEach { (mode, label) ->
                FilterChip(selected = t.commuteMode == mode,
                    onClick = { update(t.copy(commuteMode = mode, mode = TransportMode.NONE,
                        customOnPayroll = false, deductedFromNetSalary = false)) },
                    label = { Text(label) })
            }
        }
        if (t.commuteMode == CommuteMode.PUBLIC) {
        SurfaceCard {
            Text("Seu calendário de trabalho", style = MaterialTheme.typography.titleMedium)
            WizardCalendar(t, compact = true) { changed ->
                val month = WorkCalendar.month(changed.workCalendarMonth)
                val anchor = runCatching { java.time.LocalDate.parse(changed.workAnchorDate) }
                    .getOrNull() ?: month.atDay(1)
                val count = WorkCalendar.days(month, changed.workSchedule, changed.workWeekdaysMask,
                    anchor, WorkCalendar.parseDates(changed.customWorkDates),
                    WorkCalendar.parseDates(changed.workExtraDates), WorkCalendar.parseDates(changed.workOffDates)).size
                update(changed.copy(workCalendarMonth = month.toString(),
                    daysPerMonth = if (changed.workSchedule == WorkSchedule.MANUAL) changed.daysPerMonth else count))
            }
            Hint("Mudanças de escala e datas ajustam os dias do mês-base; navegar pela prévia não altera o orçamento.")
        }
        SurfaceCard {
            FieldAmount("Valor de cada passagem", t.fareCents,
                { update(t.copy(fareCents = it)) })
            FieldCount("Quantas passagens por dia?", t.ridesPerDay, 0..20,
                { update(t.copy(ridesPerDay = it)) })
            FieldCount("Quantos dias de transporte por mês?", t.daysPerMonth, 0..31,
                { update(t.copy(daysPerMonth = it, workSchedule = WorkSchedule.MANUAL)) })
            Hint("Use os dias em que realmente precisa ir ao trabalho e voltar. Inclua deslocamentos extras na quantidade diária, se necessário.")
        }
        SurfaceCard {
            Text("Quem paga o transporte?", style = MaterialTheme.typography.titleMedium)
            TransportOption("Eu pago tudo", TransportMode.NONE, t.mode,
                { update(t.copy(mode = it, deductedFromNetSalary = false)) })
            TransportOption("Vale-transporte CLT (até 6% do salário básico)", TransportMode.CLT, t.mode,
                { update(t.copy(mode = it)) })
            TransportOption("Empresa paga 100%", TransportMode.FULL, t.mode,
                { update(t.copy(mode = it)) })
            TransportOption("Empresa paga uma porcentagem", TransportMode.CUSTOM, t.mode,
                { update(t.copy(mode = it)) })
            TransportOption("Empresa paga um valor fixo", TransportMode.FIXED, t.mode,
                { update(t.copy(mode = it)) })
            if (t.mode == TransportMode.CLT) {
                FieldAmount("Salário básico (R$)", t.basicSalaryCents,
                    { update(t.copy(basicSalaryCents = it)) },
                    if (b.salary.mode == SalaryMode.GROSS) "Deixe em zero para usar automaticamente o salário bruto da central de salário." else "Use o salário básico, não o líquido.")
                if (t.basicSalaryCents <= 0 && b.salary.mode != SalaryMode.GROSS)
                    Text("Informe o salário básico. Até lá, o orçamento considera o custo integral do transporte.", color = Negative)
            }
            if (t.mode == TransportMode.CUSTOM) {
                FieldCount("Percentual pago pela empresa (%)", t.companyPercent, 0..100,
                    { update(t.copy(companyPercent = it)) })
            }
            if (t.mode == TransportMode.FIXED) {
                FieldAmount("Ajuda mensal da empresa", t.companyFixedCents,
                    { update(t.copy(companyFixedCents = it)) })
            }
            if ((t.mode == TransportMode.CUSTOM || t.mode == TransportMode.FIXED) && b.salary.mode == SalaryMode.GROSS) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = t.customOnPayroll,
                        onCheckedChange = { update(t.copy(customOnPayroll = it)) })
                    Text("Minha parte é descontada no contracheque", modifier = Modifier.weight(1f))
                }
            }
            if (t.mode != TransportMode.NONE && b.salary.mode == SalaryMode.NET) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = t.deductedFromNetSalary,
                        onCheckedChange = { update(t.copy(deductedFromNetSalary = it)) })
                    Text("Meu salário líquido já considera o desconto do transporte",
                        modifier = Modifier.weight(1f))
                }
                Hint("Se esse custo já saiu do salário líquido informado, ele não entra novamente no orçamento.")
            }
            if (b.salary.mode == SalaryMode.GROSS && t.mode == TransportMode.CLT)
                Hint("No modo salário bruto, o vale-transporte CLT já entra automaticamente na folha e não é lançado duas vezes.")
        }
        }
        if (t.commuteMode == CommuteMode.PRIVATE) {
            SurfaceCard {
                FieldAmount("Custo mensal com veículo", t.privateMonthlyCents,
                    { update(t.copy(privateMonthlyCents = it)) })
                Hint("Inclua combustível, manutenção e estacionamento conforme seu uso.")
            }
        }
        SurfaceCard {
            Text("Resumo mensal", style = MaterialTheme.typography.titleMedium)
            ValueLine(if (t.commuteMode == CommuteMode.PUBLIC) "Passagens no mês" else "Transporte mensal", gross)
            if (t.commuteMode == CommuteMode.PUBLIC) ValueLine("Parte estimada da empresa", company)
            HorizontalDivider(color = Lilac)
            ValueLine("Custo do trabalhador", employee, emphasize = true)
            if (BudgetMath.payrollTransport(b) > 0L || (b.salary.mode == SalaryMode.NET && t.commuteMode == CommuteMode.PUBLIC && t.mode != TransportMode.NONE && t.deductedFromNetSalary)) {
                Hint("Esse custo já consta como desconto no salário, sem duplicação no orçamento.")
            }
        }
        Hint("Lei 7.418/1985: no VT CLT, o trabalhador participa em até 6% do salário básico, limitado ao custo do deslocamento; o excedente é custeado pela empresa. Benefícios superiores podem existir. Confira o holerite.")
    }
}

@Composable
private fun TransportOption(label: String, mode: TransportMode, selected: TransportMode,
                            onSelect: (TransportMode) -> Unit) {
    val active = mode == selected
    Surface(
        onClick = { onSelect(mode) },
        color = if (active) Lilac else Color(0xFFF9F7FB),
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = active, onClick = { onSelect(mode) },
                modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(3.dp))
            Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = Ink)
        }
    }
}
