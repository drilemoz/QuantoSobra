# Quanto Sobra? — Notas da versão 0.1.0-alpha.3

**31 de outubro de 2026** · `versionCode 3`

- Corrigido teste unitário `BudgetMathTest.moneyLocalized`, cuja comparação misturava `Int` e `Long?` e provocava falsa falha no JUnit.
- Adicionados testes para entrada com `R$`, vazio e formato inválido.
- Código Kotlin do aplicativo compilou no GitHub Actions do build 2 (alpha.2); o APK ficou pendente devido à falha de testes, agora corrigida no código-fonte.
- Lógica financeira, telas e dados salvos permanecem iguais aos da alpha.2.

**Para validar esta versão:** execute `Gerar APK` no GitHub Actions e confira as tarefas `:app:testDebugUnitTest` e `:app:assembleDebug`. APK ainda não foi gerado ou testado nesta entrega.
