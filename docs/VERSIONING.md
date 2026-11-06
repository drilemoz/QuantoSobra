# Versionamento — Quanto Sobra?

- Atual: `0.1.0-alpha.17`, `versionCode 17`.
- Próxima alteração: `0.1.0-alpha.18`, `versionCode 18`.
- Pacote fixo: `br.com.quantosobra`.
- Sincronizar Gradle, workflow Artifact, README, CHANGELOG, RELEASE_NOTES, docs/VALIDATION e interface.
- Dados antigos em `SharedPreferences` `quanto_sobra_v1` preservados. Schema 6 inclui `workType`, `rentPeriod`, `iptuPeriod`, `foodEstimate`, transporte por modalidade. JSON v1/v2/v3 usa defaults compatíveis.
- Wizard abre em instalação limpa/reinstalação sem dados e após a limpeza interna; atualizações normais preservam o orçamento e mostram novidades uma vez por versão. A opção de refazer o assistente fica na engrenagem, preservando valores atuais até a conclusão.

- Alpha.10 preserva JSON schema 4 e anteriores ao ler, adicionando campos opcionais `unit`, `percentBP`, `percentBase` e `companyFixed`.

- Alpha.13 preserva dados antigos ao ler JSON schema 1–5; schema 6 adiciona somente campos opcionais `priceOrigin`, `extraDates` e `offDates`, mantendo os valores existentes.

- Alpha.14 adiciona schema JSON 7 opcional (`foodVA`, `foodVR`, `recurringMeals`); lê o `foodBenefit` anterior como VA e preserva toda a estrutura e listas antigas. Não exige limpeza de dados.

- Alpha.15 adiciona JSON schema 8 com `foodDiscountMethod`, `foodDeductionAlreadyInNet`, `vaForMarket`, `vaForMeals`, `vrForMarket`, `vrForMeals`. Em dados schema 1–7 os dois usos cruzados iniciam habilitados para preservar o abatimento geral anterior; em instalações novas assumem VA no mercado e VR nas refeições, permitindo correção. Desconto antigo ID 900002 permanece legível como valor fixo.

- Alpha.16 adiciona schema 9 com objeto `income` opcional: modo detalhado, receitas, custos, tributos, INSS/RPPS reais, reserva, retirada, DAS MEI, acumulado do MEI, histórico e fontes de renda extras. Os campos anteriores continuam preservados e perfis `MEI_PJ` são legíveis como MEI sem migração destrutiva. Nenhuma reinterpretação automática da renda de dados legados.
