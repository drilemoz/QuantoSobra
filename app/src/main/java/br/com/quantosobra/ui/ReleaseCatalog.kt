package br.com.quantosobra.ui

/** Fonte única das novidades exibidas na abertura e na seção Sobre. */
data class ReleaseFeature(val title: String, val detail: String)
data class ReleaseEntry(
    val version: String,
    val date: String,
    val headline: String,
    val features: List<ReleaseFeature>
)

object ReleaseCatalog {
    const val currentVersion = "0.1.0-alpha.14"
    val recent = listOf(
        ReleaseEntry("0.1.0-alpha.14", "01/11/2026", "Marmitas, benefícios e campos mais fáceis de editar", listOf(
            ReleaseFeature("Cursor e digitação", "Agora é possível tocar no meio do valor, mover o cursor e alterar dígitos sem seleção forçada ou máscaras que saltam."),
            ReleaseFeature("Marmita no calendário", "Refeições recorrentes por dia trabalhado, dia do mês ou frequência personalizada com valor mensal automático."),
            ReleaseFeature("Benefícios e descontos", "VA e VR separados, descontos adicionais editáveis no assistente e resumo sem somar cartões ao salário em dinheiro.")
        )),
        ReleaseEntry("0.1.0-alpha.13", "01/11/2026", "Sua alimentação e seu calendário, do seu jeito", listOf(
            ReleaseFeature("Produtos básicos configuráveis", "Oito itens com preços indicativos do Atacadão, embalagens e quantidades editáveis, sem sobrescrever seus produtos."),
            ReleaseFeature("Comida na mesa", "Editor de produtos diretamente no assistente e lista compacta com diferença entre estimativa e subtotal conhecido."),
            ReleaseFeature("Folgas e plantões", "Exceções por data e ciclo 4×3 ancorado, com navegação de meses separada da referência do transporte.")
        )),
        ReleaseEntry("0.1.0-alpha.12", "31/10/2026", "Seu salário, no campo certo", listOf(
            ReleaseFeature("Botão Usar corrigido", "O salário mínimo escolhido preenche o campo bruto e atualiza os descontos imediatamente."),
            ReleaseFeature("Consulta online corrigida", "Requisição ao Banco Central respeita o limite da API, mantendo a referência offline se a rede falhar."),
            ReleaseFeature("Retorno claro", "Uma mensagem confirma quando o valor foi aplicado e informa a causa básica de indisponibilidade.")
        )),
        ReleaseEntry("0.1.0-alpha.11", "31/10/2026", "Salário explicado, sem surpresas", listOf(
            ReleaseFeature("Consultar salário mínimo", "Consulta a base do Banco Central, pode editar a referência ou aplicá-la ao bruto quando quiser."),
            ReleaseFeature("Entender descontos", "Veja parcelas do INSS por faixa, percentuais e o líquido separado do salário bruto."),
            ReleaseFeature("Digitação corrigida", "Apagar dígitos de um valor com ponto de milhar não transforma reais em centavos.")
        )),
        ReleaseEntry("0.1.0-alpha.10", "31/10/2026", "Seu trabalho, seus números", listOf(
            ReleaseFeature("Escalas e calendário", "Visual com cartões 5×2, 6×1, 12×36, 4×3 ou personalizado, com dias recalculados ao toque."),
            ReleaseFeature("Compras organizadas", "Escolha produtos essenciais ou cadastre e edite preços, quantidades e unidades."),
            ReleaseFeature("Contas e salários", "Celular e gás em Outros gastos; orientações legais e descontos manuais em valor ou percentual."),
            ReleaseFeature("Dinheiro sem confusão", "A entrada 900 significa R$ 900,00, preservando reais durante a digitação.")
        )),
        ReleaseEntry("0.1.0-alpha.9", "31/10/2026", "O Quanto Sobra? evoluiu.", listOf(
            ReleaseFeature("Primeiro acesso guiado", "O assistente aparece em instalações novas e após limpar os dados."),
            ReleaseFeature("Configurações renovadas", "Todas as ações agora ficam na engrenagem do cabeçalho."),
            ReleaseFeature("Histórico de novidades", "Consulte as últimas mudanças e o contato do desenvolvedor na seção Sobre.")
        )),
        ReleaseEntry("0.1.0-alpha.8", "31/10/2026", "Seu planejamento, do seu jeito", listOf(
            ReleaseFeature("Wizard financeiro", "Onze etapas, calendário de trabalho e benefícios integrados."),
            ReleaseFeature("Valores bem formatados", "Pontuação de dinheiro durante a digitação e despesas anuais.")
        )),
    )
}
