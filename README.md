# Quanto Sobra? 💜

Aplicativo Android **nativo em Kotlin e Jetpack Compose**, pequeno e offline, para simular um orçamento mensal e responder: *quanto sobra do meu salário depois de morar, comer, pagar contas, me transportar e reservar dinheiro?*

**Versão:** `0.1.0-alpha.9` · **versionCode:** `9` · **Pacote:** `br.com.quantosobra` · **Android:** 8.0+ (API 26)

## Novidades e configurações — alpha.9

- **Instalação inicial, reinstalação limpa ou limpeza de dados do Android:** wizard abre automaticamente. Backup automático de dados continua desativado no manifesto; atualização normal preserva os dados locais.
- **Atualização de versão:** a tela de novidades aparece uma única vez, depois de completar a configuração quando aplicável; pode ser reaberta pelas Configurações. Quem veio de versão antiga sem marcador de leitura verá as novidades uma vez na alpha.9.
- **Menu discreto de engrenagem:** refazer wizard (mantendo orçamento antigo até concluir), carregar exemplo de R$ 3.000, limpar com confirmação (reinicia wizard), novidades e Sobre.
- **Sobre:** nome e versão, três últimas atualizações pela mesma fonte de dados da tela de novidades, desenvolvedor Adriedson Lemos, e-mail `drilemoz@gmail.com`, copiar endereço e abrir app de e-mail instalado.
- As novas preferências de controle de novidades ficam no mesmo SharedPreferences privado; JSON de orçamento continua no schema 4 e nenhuma chave de assinatura foi alterada.
- Não há backup/restauração; não desinstale o APK atual sem considerar a perda de dados. Para atualizações, use uma assinatura compatível.

## Assistente inicial redesenhado — alpha.8

- **11 etapas sem rolagem**: vínculo, salário, benefícios/descontos, transporte, calendário de trabalho, passagens, participação da empresa, moradia, contas domésticas, alimentação e resumo financeiro.
- Visual autoral com gradientes, formas, cartões selecionáveis, indicador de avanço e contraste. **Voltar**, **Continuar** e **Definir depois**; rascunho salvo somente ao concluir.
- **Valores em reais pontuados durante a digitação**, preservando o significado natural do número: `1600` → `1.600`, `12.50` → `12,50`, `1.234,56` → `1.234,56`; ao sair do campo, exibe duas casas decimais. Foco seleciona o texto existente para facilitar a substituição.
- Calendário local para escolher mês e escala **segunda a sexta**, **12×36** (com dia de referência), ou **marcação de datas individuais**. Os dias do mês selecionado alimentam o custo do transporte automaticamente, com opção de correção manual. Feriados e folgas não são inferidos.
- Wizard inclui **VR/VA como crédito usado**, plano de saúde, desconto de VR/VA no contracheque, outros descontos CLT e vale-transporte, com cálculo único e sem duplicação.
- Moradia já inclui **aluguel, condomínio e IPTU**, com periodicidade mensal/anual e conversão para equivalente mensal. Contas de energia, água, internet e celular entram no fluxo.
- Lista discreta de produtos essenciais sem preços fictícios. Mercado estimado continua válido até que a lista seja totalmente preenchida.
- Schema JSON **4**, retrocompatível com os dados v1, v2 e v3. Contém escala/dias selecionados e mês de referência; não altera pacote nem assinatura Release. Usuários existentes não são forçados a refazer o wizard: use engrenagem → Refazer configuração guiada.
- A simulação de INSS e IRRF 2026 é simplificada para CLT. Rendas MEI/PJ/autônomas/servidores não recebem descontos CLT arbitrários. Confira o holerite real.

## APK Release assinado e Secrets (alpha.6)

O workflow mantém o APK **Debug** normalmente, sem exigir Secrets. Em execuções por **push** ou manualmente (não em pull requests), se os quatro Secrets abaixo existirem no GitHub, ele também produz `QuantoSobra-0.1.0-alpha.9-release-assinado`, com assinatura Release estável:

- `KEYSTORE_BASE64` — bytes da keystore PKCS12 convertidos em base64, linha única.
- `KEYSTORE_PASSWORD` — senha da keystore.
- `KEY_ALIAS` — identificador da chave.
- `KEY_PASSWORD` — senha da chave (em PKCS12, aqui igual à senha da keystore).

Importar o TXT confidencial pelo **GitHub Manager → Repositório → Secrets → Importar**, revisar os nomes/valores na prévia e confirmar. O TXT e o `.p12` **não devem jamais ser commitados**. Mantenha cópia privada e segura da chave: perder a chave significa perder a capacidade de emitir atualizações compatíveis com o mesmo certificado.

**Aviso sobre atualizações:** os APKs Debug anteriores foram assinados com uma chave de depuração diferente. O APK Release com a nova keystore **não atualiza diretamente sobre eles**. Não desinstale o app sem antes salvar os dados; a função de exportação/restauração ainda não está implementada. O APK Debug mantém seu processo de assinatura anterior.

A configuração é opcional e não interfere nos cálculos de salário nem nos dados locais. A compilação Android completa e a assinatura via GitHub Actions ainda precisam ser validadas no CI.

## Central de salário (alpha.5)

- Toque em **Editar** na seção de salário para abrir o novo bottom sheet com **Já sei o líquido** ou **Calcular do bruto**. Quem já usava as versões anteriores mantém o modo líquido e os dados.
- No modo bruto, informe salário, ative/desative INSS progressivo (2026) e IRRF (2026), ajuste dependentes e adicione descontos nomeados (plano de saúde, alimentação cobrada na folha, adiantamento, contribuição autorizada, faltas etc.).
- **Vale-transporte CLT** vem da aba Transporte e entra no desconto salarial automaticamente, sem ser debitado também na categoria Transporte. No custeio personalizado, indique se é cobrado na folha.
- **VA/VR utilizado no mês** reduz os gastos de Alimentação que saem do bolso. Não vira salário em dinheiro. O valor cobrado pela empresa na folha, se houver, é um desconto manual independente.
- A simulação representa **salário mensal de um empregado CLT com vínculo único**. Não pretende reproduzir todas as regras de um contracheque real, 13º ou férias; confira sempre o holerite.
- Dados salvos localmente, sem INTERNET e com **migração retrocompatível** do JSON versão 1 para versão 2.
- Android 16: `compileSdk 36`, `targetSdk 36`, `minSdk 26` (AGP 8.10.1 / Gradle 8.11.1 / JDK 17).

## Nova experiência de interface (alpha.4)

Com base nas capturas enviadas do app instalado, todas as categorias foram convertidas de telas completas para **painéis flutuantes inferiores** (`ModalBottomSheet`) que abrem sobre o orçamento e podem ser fechados pelo botão ×, gesto ou voltar. Os formulários e confirmações agora usam diálogos com cantos arredondados e ações agrupadas. Os detalhes continuam editáveis e o total é atualizado em tempo real.

A tela inicial foi redesenhada com um painel de saldo e uma grade compacta de seis categorias. Na alpha.4, as ações **Carregar exemplo** e **Limpar dados** deixaram o rodapé e foram para o ícone `$`. A partir da alpha.9, ficam na engrenagem de Configurações, junto de Sobre e Novidades. Apagar dados ainda exige confirmação.

**Na alpha.4, os dados foram preservados:** mesmo ID do aplicativo, mesmo schema de JSON e preferências; apenas apresentação e navegação foram alteradas. Vale-transporte, alimentação, reserva e campos financeiros mantêm os cálculos da alpha.3. Não foi realizada compilação Android completa neste ambiente, portanto o novo layout e eventuais incompatibilidades de Compose ainda precisam ser validados no GitHub Actions e no dispositivo.

## Correção do build 2 (alpha.3)

O segundo workflow concluiu a instalação de SDK/Gradle e compilou `:app:compileDebugKotlin` e `:app:compileDebugUnitTestKotlin`, mas falhou em `:app:testDebugUnitTest`: 6 testes, 1 falha (`BudgetMathTest.moneyLocalized`, linha 40).

**Causa:** as expectativas `Int` do teste (`135_000`, `570`) eram comparadas a valores retornados como `Long?` pelo conversor de dinheiro, levando a comparação JUnit de tipos numéricos empacotados diferentes. O conversor foi compilado e checado isoladamente, e os valores obtidos estavam corretos. A alpha.3 padroniza as expectativas como `Long` (`135_000L` etc.) e acrescenta cenários de entrada com `R$`, vazio e entradas malformadas. Não altera cálculos do aplicativo, dados salvos ou layout.

**Status:** as tarefas de compilação do Kotlin concluíram com sucesso no CI da alpha.2; APK Debug ainda não foi gerado, pois os testes interromperam o workflow antes de `:app:assembleDebug`. Reexecutar o workflow com alpha.3.

## Correção do build 1 (alpha.2)

O primeiro workflow parou **antes do Gradle** na etapa `android-actions/setup-android@v3`: a ação tentou instalar o pacote obsoleto `tools` e `sdkmanager` retornou `Failed to find package 'tools'`.

A alpha.2 muda para `android-actions/setup-android@v4` e instala explicitamente apenas `platform-tools`, além de atualizar `checkout@v5` e `setup-java@v5` para runners Node 24. O workflow daquela versão instalava SDK 35/Build Tools 35.0.0 via `sdkmanager` e gerando o APK Debug. Não há mudanças funcionais nos cálculos desta versão.

## Funcionalidades implementadas

- Painel roxo com gradiente, saldo disponível, barra de comprometimento e categorias editáveis.
- Salário **líquido ou bruto CLT** com edição na tela inicial e assistente de primeiro acesso.
- Moradia: aluguel e IPTU com valor mensal ou anual, condomínio mensal.
- Alimentação: estimativa mensal inicial, itens essenciais opcionais sem preço, cadastro/edição/exclusão de produtos e refeições fora.
- Contas da casa: energia, água, internet, celular, gás e outras.
- Transporte: transporte público, próprio ou sem deslocamento; passagem, quantidade diária, dias/mês; pagamento próprio, pela empresa integralmente, por percentual customizado ou estimativa CLT de até 6% do salário **básico**.
- Opção para não descontar vale-transporte duas vezes se já estiver embutido no salário líquido informado.
- Gastos pessoais: saúde, higiene e limpeza, lazer, roupas, móveis e parcelas, dívidas e outros.
- Reserva mensal separada das despesas, subtraída apenas do saldo disponível.
- Exemplo de R$ 3.000 e opção para limpar dados com confirmação.
- Persistência privada via `SharedPreferences` + JSON local. **Sem login, servidor, anúncios, permissão de internet ou coleta de dados.**

O estilo é autoral, em roxo/lilás, com cartões compactos, gradientes e modais inspirados genericamente em aplicativos financeiros; não usa logotipo nem identidade de terceiros.

## Como gerar APK pelo GitHub Actions

1. Crie um repositório vazio no GitHub e envie o **conteúdo da pasta do projeto** para a raiz, incluindo `.github/workflows/android.yml`.
2. Vá a **Actions → Gerar APK → Run workflow**. Também compila em push para `main`/`master`.
3. Se o job ficar verde, abra **Artifacts → QuantoSobra-0.1.0-alpha.9-debug** e baixe o APK.
4. Instale o APK Debug no Android. Atenção: APK Debug tem assinatura automática do ambiente CI; não é chave estável para atualizações de produção.

Para abrir pelo Android Studio, use JDK 17, SDK Android 36, Android Gradle Plugin 8.10.1, Gradle 8.11.1 e Kotlin 2.1.20. O projeto **não inclui o binário de Gradle Wrapper** (`gradle-wrapper.jar`) porque não pôde ser obtido no ambiente de geração. Para uso local, instale Gradle 8.11.1 e rode `gradle :app:assembleDebug` (ou gere o wrapper via `gradle wrapper --gradle-version 8.11.1`). O GitHub Actions não depende do wrapper.

## Arquitetura

- `domain/WorkCalendar.kt`: escala semanal, 12×36 e dias escolhidos por data.
- `domain/Budget.kt`: dados, regras de orçamento, dinheiro em centavos, transporte e cálculo de produtos, sem dependência do Android.
- `data/BudgetStore.kt`: persistência local com JSON schema 4 e leitura retrocompatível.
- `ui/`: wizard compacto, dashboard Compose, modais inferiores, diálogos, campos com formatação monetária em tempo real, tema e componentes reutilizáveis.
- `MainActivity.kt`: dashboard fixo, estado único e exibição contextual de modais, sem biblioteca de navegação externa.

## Limitações atuais

- Apenas **um orçamento** por dispositivo, com projeção mensal.
- A folha CLT é estimada com INSS e IRRF 2026; não calcula férias, 13º, múltiplos vínculos nem tributos de MEI/PJ. Para os demais vínculos, informar o valor disponível após tributos conhecidos.
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
