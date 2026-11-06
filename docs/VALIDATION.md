# Validação — 0.1.0-alpha.17 (05/11/2026)

- Origem: código-fonte fornecido alpha.16. Falha exibida no Android: `java.lang.ArrayIndexOutOfBoundsException: length=3; index=3`, surgindo durante a composição/anexação de Views.
- Causa encontrada no fonte: `SettingsAndNews.kt` acessava `listOf("✦", "⚙", "↻")[index]` para as quatro `ReleaseFeature` de alpha.16. `WhatsNewScreen` é mostrada após atualizar um app já configurado.
- Correção: `ReleaseCatalog.featureIcon(index)` usa 4 ícones conhecidos e `getOrElse(index) { "✓" }` para qualquer índice fora do intervalo. Protege até novas releases que tenham mais de 4 destaques.
- Novos 3 testes JUnit: quarta novidade da alpha.16, fallback em índices excedentes e consistência do catálogo atual. Execução Gradle desses testes depende de ambiente Android/Gradle.
- `versionCode 17`, versão de interface, nomes de Artifacts, README, CHANGELOG e notas atualizados. Mantidos `applicationId`, minSdk/targetSdk e schema JSON 9; sem alterações de persistência ou assinatura.
- Pendente: `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` (Release requer assinatura quando configurada) e verificação da tela de novidades em aparelho.
