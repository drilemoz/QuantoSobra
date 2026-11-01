# Validação — 0.1.0-alpha.15 (01/11/2026)

- Base alpha.14, versionCode 15 e package ID preservado.
- Novas regras sem desconto VA/VR presumido, opções nenhum/desconhecido/percentual/fixo; referência PAT de 20% em relação ao crédito é apenas comparação informativa, pois a base legal é o custo direto global. Fonte oficial: https://www.gov.br/trabalho-e-emprego/pt-br/acesso-a-informacao/perguntas-frequentes.
- Sem dupla contagem de desconto: modo NET presume participação já embutida, com opção explícita de subtrair uma vez; modo GROSS faz desconto na folha uma vez.
- Benefícios cobrem apenas compras aceitas conforme seleção por categoria; sem somar VA/VR a renda em dinheiro, sem saldo de cartão negativo nem abatimento duplo.
- Orçamentos de schema até 7 preservam distribuição anterior por default; novos usam restrição VA/mercado, VR/refeições. Campos e desconto de VA/VR legado preservados na leitura do schema 8.
- Validação local: 54 testes de domínio Kotlin/JVM aprovados com executor de asserções compatíveis com JUnit, incluindo método de desconto, modo NET/GROSS, compatibilidade e cobertura por cartão. 33 arquivos Kotlin analisados sem erros sintáticos pelo parser PSI; **não** é compilação Compose/Android.
- Pendente: Gradle :app:testDebugUnitTest :app:assembleDebug, inspeção de acessibilidade/telas e ensaio de atualização em aparelho preservando SharedPreferences.
