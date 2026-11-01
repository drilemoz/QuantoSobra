# Validação — 0.1.0-alpha.14 (01/11/2026)

- Base: `QuantoSobra-v0.1.0-alpha.13-fonte.zip`; não substitui versão anterior no Drive nem remove configurações instaladas.
- Campo monetário: ao editar não reformatar a cada tecla, não selecionar tudo automaticamente no foco, manter `TextFieldValue` com cursor e composição de IME, formatar ao sair, salvar em centavos; conferir visual em aparelho.
- Benefícios e descontos: VA/VR separados, percentuais sobre o total dos cartões, lista editável de outros descontos na etapa 3 com valores nominais ou percentuais; sem duplicar VT. No modo líquido, não aplicar descontos que já foram incluídos no valor líquido informado.
- Marmita: recorrência por dias de trabalho (considera exceções), todos os dias, semana (projeção), mês, dias manuais, com lançamento individual e cálculo mensal no total de Alimentação; campo de extras separado.
- Persistência: JSON schema 7 com `foodVA`, `foodVR`, `recurringMeals`, compatível com schema 1–6. Ao ler orçamento antigo sem `foodVA`, preserva `foodBenefit` como VA. Sem mudanças nas permissões, assinatura ou identificação do pacote.
- Validações locais: 45 testes de domínio Kotlin/JVM usando executor de asserções compatível com JUnit (não é Gradle/JUnit oficial); parser PSI sem erros em 31 fontes Kotlin; compilação do domínio por kotlinc concluída. Conferir integridade do ZIP ao gerar.
- Pendente: compilação Android via Gradle em CI, verificações reais de layout/teclado e testes de atualização do APK preservando dados no aparelho. Não afirmar APK validado apenas pelos testes sintáticos.
