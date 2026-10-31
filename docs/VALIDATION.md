# Registro de validação — alpha.1

## Comprovado no ambiente de criação

- Compilação isolada da lógica de negócio usando o Kotlin CLI (`kotlinc`).
- Checagens locais dos cenários de orçamento, dinheiro, transporte CLT e quantidade fracionária.
- Inspeção de XML/estrutura do ZIP.

## Pendente

- `gradle :app:testDebugUnitTest :app:assembleDebug` em Android SDK real.
- Inspeção visual de interface Compose no Redmi e testes de acessibilidade.
- Persistência após reinício do aplicativo e testes de atualização.

Atenção: não confundir validação de domínio com build Android completo ou APK pronto.
