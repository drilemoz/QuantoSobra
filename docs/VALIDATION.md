# Validação — alpha.6 (31/10/2026)

## Base e compatibilidade

- Base real: ZIP de `0.1.0-alpha.4`, já instalado/testado visualmente pelo usuário.
- `BudgetStore` conserva a chave SharedPreferences `quanto_sobra_v1` e campos antigos; novo objeto `payroll` é opcional.
- Arquivo JSON passa a informar schema 2. Em leitura de schema 1 ou ausência de payroll, é usado modo NET, preservando salário informado e despesas.
- Transporte CLT usa salário básico explícito; no modo bruto, se zero, usa salário bruto configurado. Desconto do VT é contado em apenas um lugar (folha OU orçamento).
- VA/VR é crédito de uso em alimentação, limitado ao gasto alimentar, nunca somado ao salário em dinheiro.

## Checagens locais

- 12 métodos de teste Kotlin/JVM aprovados por executor de asserções compatível, sendo 6 originais e 6 novos. Não equivalem à execução JUnit via Gradle.
- Kotlin do domínio compilado com `kotlinc 1.9.0` e testes executados.
- 13 arquivos Kotlin analisados com parser PSI, sem erros de sintaxe.
- Tabelas de INSS e IRRF baseadas no INSS/Receita Federal, competência 2026. Atenção: exemplos antigos da Receita podem trazer contribuição previdenciária de outro ano; cálculos usam faixas oficiais de janeiro de 2026.
- Arquivos de workflow, XML e ZIP validados estruturalmente.

## Pendente

- `:app:testDebugUnitTest`, `:app:assembleDebug` e testes instrumentados/reais no Android.
- Testes reais de teclado em pop-ups/bottom sheets, atualização da alpha.4 sem perda, benefícios de folha e eventual mudança de comportamento ao atingir targetSdk 36.
- O modelo não é um software de folha de pagamento: não suporta múltiplos vínculos, 13º, férias, bases diferenciadas nem regras por convenção coletiva.

## Alpha.6 — assinatura Release por Secrets

- Quatro Secrets suportados, chaves PKCS12 geradas separadamente do projeto e nunca colocadas no ZIP do código.
- APK Debug permanece independente da assinatura Release; Release é gerado condicionalmente se todos os Secrets forem configurados.
- Sintaxe YAML, estrutura XML e integridade do ZIP verificadas localmente; assinatura PKCS12 verificada com keytool.
- **Pendente**: compilação Android completa, testes JUnit no CI e assinatura efetiva do APK Release com os quatro Secrets.
