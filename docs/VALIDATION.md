# Validação — 0.1.0-alpha.10 (31/10/2026)

- Base: código-fonte alpha.9 recuperado do Google Drive, com dados offline, ID e assinatura opcional mantidos.
- Domínio monetário e escalas: testes da suíte + novos casos de valores 900, dias 5×2/6×1/4×3, auxílio fixo, percentuais, prevenção de duplo desconto e preservação de custos de chip/gás.
- 30 testes aprovados pelo `kotlinc` local e executor de asserções compatível com JUnit. **Não é** a execução JUnit do Gradle.
- Análise sintática via Kotlin PSI dos arquivos Kotlin; compilação UI Compose/Android não está disponível no ambiente.
- Salário com valores percentuais aplica base identificada; PAT/VT são orientações informativas, nunca descontos silenciosos.
- Schema JSON v5 aditivo, com leitura padrão para campos ausentes; requer teste de upgrade no aparelho.
- Pendente: `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease` via CI e teste visual de calendário, teclado e pop-ups no aparelho real.
