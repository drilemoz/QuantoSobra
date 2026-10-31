# Validação — 0.1.0-alpha.12 (31/10/2026)

- Base: código-fonte alpha.11. Captura de tela da etapa 2 do wizard mostrando referência bruta e campo de salário sem preenchimento após ação **Usar**, além de mensagem de consulta online falha.
- Causa identificada no UI: `FieldAmount` não sincronizava a atualização externa enquanto o campo permanecia focado. A aplicação agora limpa foco, incrementa uma revisão externa e força a apresentação do salário escolhido, sem aplicar máscara novamente a cada dígito manual.
- Causa provável da consulta falha: `/ultimos/24` excede o limite de 20 registros do endpoint SGS/BCB; substituído por `/ultimos/12`. Resposta real no Android ainda não testada.
- Testes Kotlin/JVM de domínio, PSI sintático Kotlin, estrutura/CRC de ZIP, versão Gradle/workflow e integridade de XML e YAML são verificados localmente. **Não equivale a compilação Android real ou teste de interface no aparelho.**
- Persistência de orçamento inalterada, salário bruto distinto de líquido, rede somente opt-in.
