# Versionamento — Quanto Sobra?

- Atual: `0.1.0-alpha.7`, `versionCode 7`.
- Próxima alteração: `0.1.0-alpha.8`, `versionCode 8`.
- Pacote fixo: `br.com.quantosobra`.
- Sincronizar Gradle, workflow Artifact, README, CHANGELOG, RELEASE_NOTES, docs/VALIDATION e interface.
- Dados antigos em `SharedPreferences` `quanto_sobra_v1` preservados. Schema 3 inclui `workType`, `rentPeriod`, `iptuPeriod`, `foodEstimate`, transporte por modalidade. JSON v1/v2 usa defaults compatíveis.
- Wizard só aparece automaticamente em instalações sem orçamento existente. A opção de refazer o assistente fica no menu `$`, preservando valores atuais até a conclusão.
