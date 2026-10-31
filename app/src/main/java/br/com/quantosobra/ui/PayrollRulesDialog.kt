package br.com.quantosobra.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/** Informação educativa, sem consulta à rede ou aconselhamento jurídico. */
@Composable
fun PayrollRulesDialog(close: () -> Unit) {
    ModernDialog("Regras e benefícios", close, "Entendi", close) {
        Text("INSS e IRRF • 2026", fontWeight = FontWeight.Bold, color = Purple)
        Hint("Para um salário mensal CLT, a estimativa usa faixas progressivas e regras de redução do IR vigentes em 2026. Dependentes e situações específicas podem mudar o holerite.")
        Text("Vale-transporte • até 6%", fontWeight = FontWeight.Bold, color = Purple)
        Hint("Pela Lei 7.418/1985, o empregador paga a parcela do custo de transporte coletivo que exceder 6% do salário básico. Se o custo for menor, o desconto não ultrapassa esse custo. Acordos mais favoráveis podem existir.")
        Text("Alimentação PAT • até 20%", fontWeight = FontWeight.Bold, color = Purple)
        Hint("No Programa de Alimentação do Trabalhador (PAT), a participação do empregado no custeio é limitada a 20% do custo direto do benefício. Isso NÃO equivale a descontar automaticamente 20% do salário ou do VA/VR: informe o valor real do holerite.")
        Text("FGTS não é desconto", fontWeight = FontWeight.Bold, color = Purple)
        Hint("Depósito de responsabilidade do empregador, não reduz o salário líquido informado.")
        Text("Outros descontos", fontWeight = FontWeight.Bold, color = Purple)
        Hint("Plano de saúde, odontológico, adiantamentos e descontos autorizados não possuem um percentual nacional único. Cadastre valores ou percentuais efetivamente praticados, com base identificada.")
        Hint("Referências: Lei 7.418/1985; Decreto 10.854/2021 e orientações do Ministério do Trabalho (PAT); tabelas da Receita Federal/INSS 2026. Simulação, não substitui o contracheque.")
    }
}
