# Histórico de alterações

## 0.1.0-alpha.6 (31/10/2026)

Adicionada assinatura **Release opcional** por quatro Secrets GitHub (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). O CI continua gerando Debug sem Secrets e, quando todos estiverem presentes, decodifica a keystore temporariamente no runner, compila Release assinado, publica Artifact separado e remove o arquivo temporário. Configuração Gradle com assinatura condicional via variáveis de ambiente (nunca inclui chave/senha no código). `.gitignore` protege arquivos de assinatura e exportações de Secrets. versionCode/versionName 6, interface e docs sincronizados. Cálculos, persistência e UI da alpha.5 preservados. Build Android/assinatura real dependem da execução do CI.

## 0.1.0-alpha.5 (31/10/2026)

Central de holerite em painel flutuante: modo líquido legado ou salário bruto com INSS progressivo 2026, IRRF 2026, dependentes e descontos adicionais por nome/valor (plano de saúde, coparticipações, adiantamentos etc.). Vale-transporte CLT vindo da tela Transporte é calculado na folha e removido do orçamento por fora para evitar duplicações; customização entre desconto em folha ou pagamento por fora. VA/VR utilizado no mês é conectado à alimentação, sem somar crédito como salário em dinheiro. Dados da alpha.4 preservados com leitura retrocompatível e schema JSON 2. Android 16 (compileSdk/targetSdk 36), Gradle/AGP compatíveis, versionCode 5 e Artifact sincronizados. Testes locais de domínio e sintaxe Kotlin realizados, build Android em CI pendente.

## 0.1.0-alpha.4 (31/10/2026)

Redesign do orçamento a partir de capturas reais: categorias em grade compacta, cartão de saldo com indicador de comprometimento, cabeçalho em gradiente e editores de todas as categorias em bottom sheets, sem navegação para páginas completas. Edição de salário, produtos, extras e confirmações usa pop-ups personalizados. O ícone `$` discreto no topo abre opções com nome/versão, carregar exemplo e limpeza confirmada. Mantidos pacote, persistência, schema e regras financeiras, preservando valores existentes. `versionCode 4`, `versionName 0.1.0-alpha.4`, documentação e Artifact sincronizados. APK/visual ainda pendentes de build e teste físico.

## 0.1.0-alpha.3 (31/10/2026)

Correção do **build 2**: o SDK Android e a compilação Kotlin passaram, mas `:app:testDebugUnitTest` falhou em `BudgetMathTest.moneyLocalized` (linha 40) por comparação JUnit de `Int` com o retorno monetário `Long?`. Asserções normalizadas para `Long` e casos adicionais para `R$`, campo vazio e entrada malformada. Sem alterações na lógica de orçamento, persistência ou interface, exceto número de versão. `versionCode 3`, `versionName`, painel, nome do Artifact, README e documentação sincronizados. APK aguarda validação do próximo GitHub Actions.

## 0.1.0-alpha.2 (31/10/2026)

Correção do workflow **Gerar APK 1**: migração `android-actions/setup-android@v3` → `@v4` com instalação exclusiva de `platform-tools`, evitando pacote removido `tools` que causou falha na etapa do SDK. Atualização de actions/checkout e setup-java para v5, sincronização de versionCode 2, versão visível no painel e nome do APK Artifact. Mantidas as funcionalidades e o schema da alpha.1. Ainda depende de build CI para verificar Kotlin/Compose e APK.


## 0.1.0-alpha.1 (31/10/2026)

Primeira versão do projeto. Dashboard financeiro, edição de salário líquido, aluguel, alimentação por produtos, contas domésticas, transporte com quatro modelos de custeio, extras, reserva mensal, exemplo, limpeza confirmada e persistência local. Workflow CI gera APK Debug e executa testes unitários quando disponível. Compilação Android e testes físicos ainda não executados neste ambiente.
