# Histórico de alterações

## 0.1.0-alpha.2 (31/10/2026)

Correção do workflow **Gerar APK 1**: migração `android-actions/setup-android@v3` → `@v4` com instalação exclusiva de `platform-tools`, evitando pacote removido `tools` que causou falha na etapa do SDK. Atualização de actions/checkout e setup-java para v5, sincronização de versionCode 2, versão visível no painel e nome do APK Artifact. Mantidas as funcionalidades e o schema da alpha.1. Ainda depende de build CI para verificar Kotlin/Compose e APK.


## 0.1.0-alpha.1 (31/10/2026)

Primeira versão do projeto. Dashboard financeiro, edição de salário líquido, aluguel, alimentação por produtos, contas domésticas, transporte com quatro modelos de custeio, extras, reserva mensal, exemplo, limpeza confirmada e persistência local. Workflow CI gera APK Debug e executa testes unitários quando disponível. Compilação Android e testes físicos ainda não executados neste ambiente.
