# Registro de validação — alpha.3

## Evidência recebida do workflow Gerar APK 2

- `QuantoSobra-Gerar-APK-2-logs.zip`, execução de 31/10/2026, indica que Android SDK 35 e Gradle 8.11.1 foram preparados.
- Tarefas `:app:compileDebugKotlin` e `:app:compileDebugUnitTestKotlin` executadas sem erros.
- **Falha real:** `:app:testDebugUnitTest` com `6 tests completed, 1 failed` / `BudgetMathTest > moneyLocalized FAILED`, linha 40.
- Causa: comparação de `Int` no esperado com `Long?` no valor real, selecionando igualdade por objetos no JUnit. O conversor isolado devolve `135000` centavos para `1.350,00` e `570` para `5,70`.
- Warning sobre Node.js 20 do `setup-gradle@v4` não provocou falha; não é o erro deste workflow.

## Correções da alpha.3

- Expectativas monetárias do teste `moneyLocalized` passam a ser literais `Long` com `L`.
- Acrescentadas asserções para `R$ 1.350,00`, string vazia e formato monetário malformado.
- versionCode, versionName, UI, artifact e documentação sincronizados.
- Sem mudanças no domínio, persistência e esquema de dados; atualização preserva os dados existentes.

## Pendente

- Validar em runner real `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug` na alpha.3.
- Inspeção visual em Android e testes de persistência após atualização.
- Assinatura de APK Debug criada pelo CI pode não ser estável entre runners; não desinstalar versão anterior sem preservar dados.
