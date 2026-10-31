# Validação — 0.1.0-alpha.8 (31/10/2026)

- Base real: fonte 0.1.0-alpha.7, mantendo namespace `br.com.quantosobra`, build Release assinado opcional, SharedPreferences `quanto_sobra_v1` e dados anteriores.
- JSON schema 4: campos opcionais `workSchedule`, `weekdays`, `calendarMonth`, `anchorDate`, `customDates`. Leitor aceita ausências (JSON v1–v3), mantendo `daysPerMonth` legado.
- Widget monetário usa formatação em tempo real, seleciona todo o texto ao receber foco e não converte inteiros para centavos por deslocamento.
- Wizard utiliza coluna não rolável e oculta cabeçalho/rodapé durante IME; alturas e fontes grandes ainda devem ser verificadas em aparelhos reais.
- Calendário usa mês real do dispositivo, não pressupõe feriados, valida dias e recalcula ao mudar escala/mês.
- Benefícios e descontos CLT mantidos separados do vale-transporte, sem dupla subtração. Valores de alimentação não são renda em dinheiro.

## Testes locais

- Kotlin/JVM: **20 testes aprovados** em executor de asserções compatíveis com JUnit, incluindo novos testes para formato dinâmico, calendário semanal, 12×36, datas livres e desconto VT.
- PSI Kotlin: **13 arquivos no código principal, 0 erros sintáticos** (análise de sintaxe, não compilação Android).
- YAML, XML, integridade e CRC do ZIP, coerência de versão: verificação local após empacotamento.

## Pendências

- `:app:assembleDebug`, `:app:assembleRelease` e `:app:testDebugUnitTest` reais ainda precisam passar no GitHub Actions.
- Testar responsividade, fontes ampliadas, teclado, mudança de mês, seleção de dias no calendário e atualização sobre a alpha.7 com dados reais no aparelho.
- Cálculos tributários e de benefício são simulações simplificadas; condições específicas podem alterar o holerite.
