# Validação — 0.1.0-alpha.13 (01/11/2026)

- Base: ZIP `QuantoSobra-20261101-104349-main.zip`, versão alpha.12.
- Mudanças: editor de produtos no wizard e no painel Alimentação, seleção de preços indicativos com quantidades, layout compacto, exceções no calendário, ciclo 4×3 e mês de referência do transporte explicitamente selecionado.
- Fonte dos preços iniciais: catálogo online `https://www.atacadao.com.br/catalogo` pesquisado em 01/11/2026; **estimativas não são anúncios válidos para qualquer loja**. Nenhuma solicitação automática ao Atacadão foi implementada.
- Verificações em ambiente local: 39 testes Kotlin/JVM aprovados (executor de asserções compatível com JUnit, não o Gradle/JUnit oficial); parser PSI passou em 28 fontes Kotlin; compilação isolada do domínio pelo kotlinc aprovada. Estrutura JSON e CRC ZIP conferidas.
- **Não há compilação Android real/instalação em celular confirmada até que Gradle e UI no aparelho sejam testados**.
- Persistência: schema 6, retrocompatível com schema 1–5, sem limpar dados, IDs ou assinatura. Campos opcionais: `priceOrigin`, `extraDates`, `offDates`.
