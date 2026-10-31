# Histórico de alterações

## 0.1.0-alpha.8 (31/10/2026)

Redesign do wizard para 11 etapas sem rolagem, cartões selecionáveis, cabeçalho gradiente e resumo do orçamento. Formatação monetária durante a digitação com seleção do valor anterior ao focar, sem deslocamento automático de centavos. Calendário de trabalho com semana padrão, escala 12×36 e marcação manual de datas; apuração de dias para o mês selecionado integrada ao transporte. Etapa inicial de benefícios/descontos: VA/VR utilizado, desconto de vale-alimentação na folha, plano de saúde e outros descontos CLT. Moradia com aluguel mensal/anual, condomínio e IPTU mensal/anual; contas domésticas já no assistente. Alimentação com estimativa, refeições e lista de essenciais sem preços inventados. Persistência schema 4 retrocompatível com schema 1–3, mantendo package ID, Secrets e chave de assinatura. Testes locais de domínio (20) e análise PSI de Kotlin; compilação Android completa e validação de tela/teclado ainda pendentes.

## 0.1.0-alpha.7 (31/10/2026)

Primeiro acesso com wizard de 8 passos compactos sem rolagem, perguntas de vínculo, renda, deslocamento, vale-transporte, aluguel, alimentação e prévia do saldo. Botões Voltar/Continuar/Definir depois, com seleção de transporte e benefícios sincronizada ao orçamento. Aluguel e IPTU anuais agora são convertidos em equivalente mensal automaticamente; transporte próprio tem custo mensal. Na alimentação, estimativa inicial convive sem dupla contagem com produtos detalhados; ação discreta acrescenta nomes de alimentos essenciais com preço zero, para o usuário completar. Valores `12`, `12.00`, `12,00` aceitos e formatados com pontuação brasileira após edição em campos e diálogos. Persistência JSON schema 3 com defaults para versões antigas; menu `$` permite reabrir o assistente. Mantido workflow de assinatura opcional, sem novas credenciais. `versionCode 7`, documentação e nomes dos Artifacts sincronizados. Compilação real no Android/GitHub Actions pendente.


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
