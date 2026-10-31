package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
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
    val employee = BudgetMath.transportEmployeeCost(t)
    val company = gross - employee

    Page("Transporte", "Passagens e benefícios da empresa", back) {
        SectionTotal("Descontado do saldo disponível", BudgetMath.transportFromAvailable(t))
        SurfaceCard {
            FieldAmount("Valor de cada passagem", t.fareCents,
                { update(t.copy(fareCents = it)) })
            FieldCount("Quantas passagens por dia?", t.ridesPerDay, 0..20,
                { update(t.copy(ridesPerDay = it)) })
            FieldCount("Quantos dias de transporte por mês?", t.daysPerMonth, 0..31,
                { update(t.copy(daysPerMonth = it)) })
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
            if (t.mode == TransportMode.CLT) {
                FieldAmount("Salário básico para calcular os 6%", t.basicSalaryCents,
                    { update(t.copy(basicSalaryCents = it)) },
                    "O cálculo geral usa o salário básico, não o salário líquido. Verifique a regra do seu vínculo.")
                if (t.basicSalaryCents <= 0) Text("Informe o salário básico. Até lá, o orçamento considera o custo integral do transporte.", color = Negative)
            }
            if (t.mode == TransportMode.CUSTOM) {
                FieldCount("Percentual pago pela empresa (%)", t.companyPercent, 0..100,
                    { update(t.copy(companyPercent = it)) })
            }
            if (t.mode != TransportMode.NONE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = t.deductedFromNetSalary,
                        onCheckedChange = { update(t.copy(deductedFromNetSalary = it)) })
                    Text("Meu salário líquido já considera o desconto do transporte",
                        modifier = Modifier.weight(1f))
                }
                Hint("Se já saiu do contracheque antes do valor líquido que você informou, o app não desconta de novo. O custo real continua aparecendo abaixo.")
            }
        }
        SurfaceCard {
            Text("Resumo mensal", style = MaterialTheme.typography.titleMedium)
            ValueLine("Passagens no mês", gross)
            ValueLine("Parte estimada da empresa", company)
            HorizontalDivider(color = Lilac)
            ValueLine("Custo do trabalhador", employee, emphasize = true)
            if (t.mode != TransportMode.NONE && t.deductedFromNetSalary) {
                Hint("Esse custo já está refletido no salário líquido: adicional no orçamento = R$ 0.")
            }
        }
        Hint("Simulação informativa: o vale-transporte CLT tem regras específicas, inclusive sobre dias elegíveis, salário básico e acordos aplicáveis. O valor real do contracheque pode ser diferente.")
    }
}

@Composable
private fun TransportOption(label: String, mode: TransportMode, selected: TransportMode,
                            onSelect: (TransportMode) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = mode == selected, onClick = { onSelect(mode) })
        Text(label, modifier = Modifier.weight(1f))
    }
}
