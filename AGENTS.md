# Instruções para evoluções do Quanto Sobra?

- Aplicativo Kotlin nativo e Jetpack Compose. Manter pequeno, offline, sem dependências desnecessárias.
- Sempre que modificar o projeto, incrementar sequencialmente `versionCode` e `versionName` (alpha.1 -> alpha.2 etc.) e sincronizar `README`, `CHANGELOG`, `docs/VERSIONING.md`, nome do Artifact no workflow e notas de versão.
- Não alterar o ID `br.com.quantosobra` sem solicitação explícita.
- Não inventar testes de APK. Distinguir testes Kotlin/JVM da compilação Android e de testes em aparelho real.
- Armazenar dinheiro em centavos inteiros, evitar `Double` em aritmética monetária. Não duplicar descontos de vale-transporte.
- Preservar dados locais em migrações e implementar importação/exportação antes de mudanças incompatíveis do schema.
- Não coletar informações financeiras. INTERNET foi aprovado pelo usuário na alpha.11 apenas para consulta opt-in à série 1619 do BCB; manter o orçamento local e sem telemetria.
- Manter interface acessível, textos pt-BR e visual original em roxo/lilás.
