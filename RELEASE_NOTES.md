# Quanto Sobra? — 0.1.0-alpha.14

**01/11/2026 · versionCode 14**

- Editar valores monetários agora permite mover o cursor, corrigir apenas alguns dígitos e inserir valores no meio sem seleção forçada nem salto da máscara. A formatação aparece ao sair do campo.
- Marmita e outras refeições recorrentes: nome, valor, frequência e quantidade editáveis tanto no wizard quanto em Alimentação, com apuração pelos dias da escala e exceções.
- Alimentação mostra separadamente supermercado, refeições recorrentes, extras e total; a estimativa do mercado não é duplicada.
- Vale-alimentação e vale-refeição separados na etapa 3; descontos adicionais (nome, valor ou %) editáveis. Benefícios não são tratados como salário em dinheiro. Não repetir vale-transporte.
- Migração não destrutiva, schema 7, ID, permissões e assinatura preservados.
- Testes de domínio Kotlin/JVM e sintaxe aprovados; build Gradle/Android real e testes com teclado físico/virtual dependem de CI e aparelho.
