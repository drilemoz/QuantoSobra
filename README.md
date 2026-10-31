# Quanto Sobra? 💜

Aplicativo Android **nativo em Kotlin e Jetpack Compose**, pequeno e offline, para simular um orçamento mensal e responder: *quanto sobra do meu salário depois de morar, comer, pagar contas, me transportar e reservar dinheiro?*

**Versão:** `0.1.0-alpha.3` · **versionCode:** `3` · **Pacote:** `br.com.quantosobra` · **Android:** 8.0+ (API 26)

## Correção do build 2 (alpha.3)

O segundo workflow concluiu a instalação de SDK/Gradle e compilou `:app:compileDebugKotlin` e `:app:compileDebugUnitTestKotlin`, mas falhou em `:app:testDebugUnitTest`: 6 testes, 1 falha (`BudgetMathTest.moneyLocalized`, linha 40).

**Causa:** as expectativas `Int` do teste (`135_000`, `570`) eram comparadas a valores retornados como `Long?` pelo conversor de dinheiro, levando a comparação JUnit de tipos numéricos empacotados diferentes. O conversor foi compilado e checado isoladamente, e os valores obtidos estavam corretos. A alpha.3 padroniza as expectativas como `Long` (`135_000L` etc.) e acrescenta cenários de entrada com `R$`, vazio e entradas malformadas. Não altera cálculos do aplicativo, dados salvos ou layout.

**Status:** as tarefas de compilação do Kotlin concluíram com sucesso no CI da alpha.2; APK Debug ainda não foi gerado, pois os testes interromperam o workflow antes de `:app:assembleDebug`. Reexecutar o workflow com alpha.3.

## Correção do build 1 (alpha.2)

O primeiro workflow parou **antes do Gradle** na etapa `android-actions/setup-android@v3`: a ação tentou instalar o pacote obsoleto `tools` e `sdkmanager` retornou `Failed to find package 'tools'`.

A alpha.2 muda para `android-actions/setup-android@v4` e instala explicitamente apenas `platform-tools`, além de atualizar `checkout@v5` e `setup-java@v5` para runners Node 24. O Gradle continua instalando o SDK 35/Build Tools 35.0.0 via `sdkmanager` e gerando o APK Debug. Não há mudanças funcionais nos cálculos desta versão.

## Funcionalidades implementadas

- Painel roxo com gradiente, saldo disponível, barra de comprometimento e categorias editáveis.
- Salário **líquido** com edição direta na tela inicial.
- Moradia: aluguel, condomínio e IPTU mensal.
- Alimentação: cadastro, edição e exclusão de produtos, preço unitário e quantidade mensal fracionária; também refeições fora.
- Contas da casa: energia, água, internet, celular, gás e outras.
- Transporte: valor da passagem, quantidade diária, dias/mês; pagamento próprio, pela empresa integralmente, por percentual customizado ou estimativa CLT de até 6% do salário **básico**.
- Opção para não descontar vale-transporte duas vezes se já estiver embutido no salário líquido informado.
- Gastos pessoais: saúde, higiene e limpeza, lazer, roupas, móveis e parcelas, dívidas e outros.
- Reserva mensal separada das despesas, subtraída apenas do saldo disponível.
- Exemplo de R$ 3.000 e opção para limpar dados com confirmação.
- Persistência privada via `SharedPreferences` + JSON local. **Sem login, servidor, anúncios, permissão de internet ou coleta de dados.**

O estilo é autoral, com a combinação roxo/lilás e cartões compactos inspirada genericamente em interfaces de bancos digitais; não usa marca, logo ou recursos de terceiros.

## Como gerar APK pelo GitHub Actions

1. Crie um repositório vazio no GitHub e envie o **conteúdo da pasta do projeto** para a raiz, incluindo `.github/workflows/android.yml`.
2. Vá a **Actions → Gerar APK → Run workflow**. Também compila em push para `main`/`master`.
3. Se o job ficar verde, abra **Artifacts → QuantoSobra-0.1.0-alpha.3-debug** e baixe o APK.
4. Instale o APK Debug no Android. Atenção: APK Debug tem assinatura automática do ambiente CI; não é chave estável para atualizações de produção.

Para abrir pelo Android Studio, use JDK 17, SDK Android 35, Android Gradle Plugin 8.10.1, Gradle 8.11.1 e Kotlin 2.1.20. O projeto **não inclui o binário de Gradle Wrapper** (`gradle-wrapper.jar`) porque não pôde ser obtido no ambiente de geração. Para uso local, instale Gradle 8.11.1 e rode `gradle :app:assembleDebug` (ou gere o wrapper via `gradle wrapper --gradle-version 8.11.1`). O GitHub Actions não depende do wrapper.

## Arquitetura

- `domain/Budget.kt`: dados, regras de orçamento, dinheiro em centavos, transporte e cálculo de produtos, sem dependência do Android.
- `data/BudgetStore.kt`: persistência local com JSON e limites de leitura.
- `ui/`: telas Compose, campos, tema, painel principal e editores.
- `MainActivity.kt`: navegação simples e estado único, sem biblioteca de navegação externa.

## Limitações atuais

- Apenas **um orçamento** por dispositivo, em valores mensais.
- Salário líquido informado manualmente: não calcula INSS, IR, férias ou 13º.
- Estimativa simplificada de vale-transporte CLT; acordos, jornadas e regras particulares podem alterar o desconto.
- Ainda sem exportação/importação de backup, gráficos históricos, notificações ou sincronização.
- **Custos pontuais de mobília/caução** ainda não possuem calendário de ocorrência próprio. Parcelas mensais podem ser lançadas em “Móveis e parcelas”.
- Valores de exemplo não são cotações nem representam gastos reais de um usuário.

## Testes e validação

- Cálculos isolados compilados com `kotlinc`, com verificações de saldo, vale-transporte e alimentos.
- `app/src/test/.../BudgetMathTest.kt`: testes JUnit, executados no CI com `:app:testDebugUnitTest`.
- **Build completo do Android e execução em aparelho ainda pendentes** neste ambiente, pois não há SDK Android, Gradle nem acesso à internet para dependências. O build 1 falhou na preparação do SDK, corrigida na alpha.2. O build 2 compilou fontes Android/Kotlin, mas parou no teste JUnit `moneyLocalized` corrigido na alpha.3; o APK permanece pendente de novo workflow.

## Licença

Código-fonte entregue para evolução do projeto. Terceiros devem respeitar licenças das dependências e do Android SDK.
