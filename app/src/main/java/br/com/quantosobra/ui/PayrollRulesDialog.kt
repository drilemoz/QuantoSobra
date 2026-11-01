package br.com.quantosobra.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.quantosobra.domain.*

/** Explicação contextual baseada nas faixas de 2026; sem tratar taxa marginal como taxa única. */
@Composable
fun PayrollRulesDialog(budget: Budget? = null, close: () -> Unit) {
    ModernDialog("De onde vêm os descontos?", close, "Entendi", close) {
        Text("Salário mínimo ≠ salário líquido", fontWeight = FontWeight.Bold, color = Purple)
        Hint("O mínimo nacional é divulgado em valor BRUTO. Para CLT, o aplicativo estima os descontos de INSS, IRRF e vale-transporte, conforme os dados configurados.")
        Text("INSS progressivo • faixas de 2026", color = Purple, fontWeight = FontWeight.Bold)
        Hint("7,5% até R$ 1.621,00; 9% de R$ 1.621,01 até R$ 2.902,84; 12% até R$ 4.354,27; 14% até R$ 8.475,55. Cada porcentagem incide apenas sobre a parcela na respectiva faixa.")
        if (budget != null && budget.salary.mode == SalaryMode.GROSS) {
            val gross = budget.salary.grossCents
            val d = SalaryMath.breakdown(budget)
            Text("Simulação com seu bruto: ${Money.format(gross)}", color = Ink, fontWeight = FontWeight.SemiBold)
            SalaryMath.inssTiers2026(gross).forEach { tier ->
                if (tier.portionCents > 0L) {
                    Text("${tier.rateLabel}: ${Money.format(tier.portionCents)} da faixa → ${Money.format(tier.deductionCents)}",
                        fontSize = 12.sp, color = Muted)
                }
            }
            Text("INSS descontado: ${Money.format(d.inssCents)}" +
                if (!budget.salary.autoInss) " (cálculo desativado)" else "", fontWeight = FontWeight.Bold, color = Ink)
            val effective = if (gross > 0L) "%.2f".format(java.util.Locale.forLanguageTag("pt-BR"), d.inssCents.toDouble() * 100.0 / gross) else "0,00"
            Hint("Alíquota efetiva do INSS sobre seu salário: $effective% (não é a última alíquota marginal).")
            Text("IRRF estimado: ${Money.format(d.irrfCents)}", fontWeight = FontWeight.SemiBold, color = Ink)
            val base = SalaryMath.irrfBase2026(gross, d.inssCents, budget.salary.dependents)
            Hint("Base estimada do IRRF: ${Money.format(base)}. A tabela começa a tributar bases acima de R$ 2.428,80, mas há deduções e redução adicional de IR para rendas dentro dos limites legais de 2026.")
            Text("Vale-transporte na folha: ${Money.format(d.transportCents)}", color = Ink, fontWeight = FontWeight.SemiBold)
            Text("Demais descontos: ${Money.format(d.otherCents)}", color = Ink, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(color = Lilac)
            Text("Total descontado: ${Money.format(d.totalDeductionsCents)}", color = Purple, fontWeight = FontWeight.Bold)
            Text("Líquido estimado: ${Money.format(d.netCents)}", color = Purple, fontWeight = FontWeight.Bold)
        }
        Text("Vale-transporte • até 6%", color = Purple, fontWeight = FontWeight.Bold)
        Hint("Na regra geral CLT, a contribuição do trabalhador é limitada a 6% do salário básico e ao custo efetivo do transporte coletivo. Condições mais favoráveis podem existir.")
        Text("Vale-alimentação e refeição • PAT", color = Purple, fontWeight = FontWeight.Bold)
        Hint("No PAT, a participação é limitada a 20% do custo direto, com apuração global no período aplicável (até 12 meses). Esse custo pode diferir do crédito recebido; não presuma cobrança automática. Confira o holerite e a norma coletiva.")
        Text("FGTS não é desconto do empregado", color = Purple, fontWeight = FontWeight.Bold)
        Hint("Plano de saúde, outros benefícios e descontos autorizados não possuem um percentual nacional único. Confira seu contracheque.")
        Hint("Referências: INSS/Receita 2026; Lei 7.418/1985; Ministério do Trabalho (PAT). São estimativas e não substituem o holerite.")
    }
}
