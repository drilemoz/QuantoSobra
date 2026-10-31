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
    const val currentVersion = "0.1.0-alpha.9"
    val recent = listOf(
        ReleaseEntry("0.1.0-alpha.9", "31/10/2026", "O Quanto Sobra? evoluiu.", listOf(
            ReleaseFeature("Primeiro acesso guiado", "O assistente aparece em instalações novas e após limpar os dados."),
            ReleaseFeature("Configurações renovadas", "Todas as ações agora ficam na engrenagem do cabeçalho."),
            ReleaseFeature("Histórico de novidades", "Consulte as últimas mudanças e o contato do desenvolvedor na seção Sobre.")
        )),
        ReleaseEntry("0.1.0-alpha.8", "31/10/2026", "Seu planejamento, do seu jeito", listOf(
            ReleaseFeature("Wizard financeiro", "Onze etapas, calendário de trabalho e benefícios integrados."),
            ReleaseFeature("Valores bem formatados", "Pontuação de dinheiro durante a digitação e despesas anuais.")
        )),
        ReleaseEntry("0.1.0-alpha.7", "31/10/2026", "Seu primeiro orçamento guiado", listOf(
            ReleaseFeature("Passo a passo", "Configuração em etapas sem rolagem, com opção Definir depois."),
            ReleaseFeature("Contas mais simples", "Aluguel e IPTU anuais, produtos essenciais opcionais.")
        ))
    )
}
