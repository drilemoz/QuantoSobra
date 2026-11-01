# Validação — 0.1.0-alpha.16 (01/11/2026)

- Base alpha.15, versionCode 16, package ID br.com.quantosobra preservado.
- JSON schema 9 adiciona objeto `income` com campos opcionais de faturamento, custos, tributos, reservas, retirada, DAS/MEI, histórico e fontes de renda; dados JSON antigos permanecem legíveis. Teste completo de migração em aparelho ainda pendente.
- Testes de domínio Kotlin/JVM locais: 67 casos aprovados (54 preexistentes e 13 novos), com executor de asserções compatíveis com JUnit. Não equivale a execução Gradle/Android.
- Parser PSI Kotlin: 35 arquivos em `app/src` sem erros sintáticos. Não comprova resolução de tipos Compose.
- Valores DAS 2026 baseados na Receita Federal (R$ 82,05; R$ 86,05; R$ 87,05) e teto geral de R$ 81.000/ano, proporcional a R$ 6.750 por mês ativo. Cálculos não contemplam MEI caminhoneiro.
- Salário e transporte CLT não são aplicados a MEI/PJ/autônomo/servidor; tipo UNKNOWN conserva cálculo legado para orçamentos antigos sem categoria.
- Pendente: `gradle :app:testDebugUnitTest :app:assembleDebug` em ambiente com Gradle/Android SDK e teste visual/teclado no aparelho.
