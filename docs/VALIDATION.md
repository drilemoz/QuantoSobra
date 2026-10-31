# Validação — 0.1.0-alpha.11 (31/10/2026)

- Base: alpha.10 real. Vídeo recebido de 29,9 segundos analisado: ponto de milhar convertido indevidamente em separador decimal após backspace.
- 36/36 testes locais Kotlin/JVM aprovados com runner JUnit compatível; testes adicionais de regressão monetária, faixas INSS, mínimo bruto vs líquido, JSON SGS e datas futuras.
- Análise Kotlin PSI de todas as fontes `.kt` sem erros sintáticos. Manifest com INTERNET para consulta opt-in.
- Não houve compilação Android real, teste de HTTPS no aparelho nem build Release; `:app:testDebugUnitTest`, `:app:assembleDebug` e `:app:assembleRelease` dependem do CI. Conferir interações e máscaras no teclado Android.
- Cache do salário mínimo é separado dos dados financeiros; não altera salário do usuário sem ação explícita.
