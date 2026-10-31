# Registro de validação — alpha.2

## Comprovado no ambiente de criação

- Compilação isolada da lógica de negócio usando o Kotlin CLI (`kotlinc`).
- Checagens locais dos cenários de orçamento, dinheiro, transporte CLT e quantidade fracionária.
- Inspeção de XML/estrutura do ZIP.

## Diagnóstico do primeiro workflow

- Logs `QuantoSobra-Gerar-APK-1-logs.zip`: etapa `Android SDK 35` (`android-actions/setup-android@v3`) tentou instalar `tools`, pacote removido; falhou com `sdkmanager` código 1. **Nenhuma tarefa Gradle/Kotlin chegou a ser executada.**
- Corrigido na alpha.2 migrando para `android-actions/setup-android@v4` e `packages: 'platform-tools'`.

## Validação local da alpha.2

- Lógica de domínio compilada com `kotlinc` CLI 1.9, com **18 asserções** independentes para saldo, descontos, refeições e valores.
- Workflow YAML analisado e configuração `setup-android@v4` + `packages: platform-tools` confirmada.
- XML do projeto analisado; versionamento (`versionName`, `versionCode`, artifact e UI) consistente.
- Não foi possível executar Gradle/Compose nem gerar APK localmente por falta de Gradle, SDK Android e bibliotecas Android neste ambiente.

## Pendente

- `gradle :app:testDebugUnitTest :app:assembleDebug` em Android SDK real.
- Inspeção visual de interface Compose no Redmi e testes de acessibilidade.
- Persistência após reinício do aplicativo e testes de atualização.

Atenção: não confundir validação de domínio com build Android completo ou APK pronto.
