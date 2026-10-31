# Registro de validação — alpha.4 (31/10/2026)

## Origem

- Base real da versão `0.1.0-alpha.3` preservada a partir do ZIP existente no Google Drive.
- Capturas de tela do usuário evidenciaram navegação por telas completas, cartões altos e ações de exemplo/limpeza/versionamento expostas no rodapé.

## Alterações verificáveis

- MainActivity mantém dashboard montado ao fundo e abre exatamente um editor de categoria conforme o identificador da rota.
- `Page()` agora renderiza `ModalBottomSheet` com scroll, altura adaptativa e ação × de fechar.
- `ModernDialog()` fornece padrão único para edição/confirmações; telas Food, Extras e salário adotaram o padrão.
- As ações de carregar exemplo, limpar orçamento e informações da versão foram movidas para um menu discreto no cabeçalho, com confirmação para ações destrutivas.
- Modelo `Budget` e `BudgetStore` intactos, evitando alteração de schema e perda de dados ao atualizar.

## Limitações

- SDK Android e Gradle não disponíveis no ambiente local: não foi possível compilar Compose/Android ou conferir a interface rodando.
- Verificações locais de sintaxe/estrutura e execução isolada das regras não substituem `:app:testDebugUnitTest` e `:app:assembleDebug` no GitHub Actions.
- É necessário conferir os bottom sheets, foco de teclado, rolagem e valores no dispositivo, sobretudo em telas pequenas.
- APK Debug do CI pode ser assinado com chave diferente entre ambientes: evitar desinstalar apps com orçamento importante sem backup externo.
