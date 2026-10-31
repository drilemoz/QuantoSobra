# Versionamento — Quanto Sobra?

- Atual: `0.1.0-alpha.11`, `versionCode 11`.
- Próxima alteração: `0.1.0-alpha.11`, `versionCode 11`.
- Pacote fixo: `br.com.quantosobra`.
- Sincronizar Gradle, workflow Artifact, README, CHANGELOG, RELEASE_NOTES, docs/VALIDATION e interface.
- Dados antigos em `SharedPreferences` `quanto_sobra_v1` preservados. Schema 5 inclui `workType`, `rentPeriod`, `iptuPeriod`, `foodEstimate`, transporte por modalidade. JSON v1/v2/v3 usa defaults compatíveis.
- Wizard abre em instalação limpa/reinstalação sem dados e após a limpeza interna; atualizações normais preservam o orçamento e mostram novidades uma vez por versão. A opção de refazer o assistente fica na engrenagem, preservando valores atuais até a conclusão.

- Alpha.10 preserva JSON schema 4 e anteriores ao ler, adicionando campos opcionais `unit`, `percentBP`, `percentBase` e `companyFixed`.
