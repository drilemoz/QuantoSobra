# Quanto Sobra? — 0.1.0-alpha.7

**31/10/2026 · versionCode 7**

- Assistente de primeiro acesso com 8 etapas, sem rolagem entre campos. Botões Voltar, Continuar e Definir depois, cada etapa de preenchimento é curta.
- Vínculo CLT, MEI/PJ, autônomo/informal, servidor público e outra renda. Cálculo automático da folha de pagamento apenas para CLT; outros vínculos são registrados sem descontos presumidos.
- Transporte público, carro/moto, bicicleta/pé e home office; vale-transporte CLT sincronizado com salário para não debitar duas vezes.
- Aluguel e IPTU com opção mensal/anual: equivalente mensal calculado pelo app.
- Alimentação com estimativa inicial e botão discreto de produtos essenciais sem preços inventados; preços reais cadastrados manualmente.
- Campos monetários aceitam `12`, `12.00` e `12,00` e ficam pontuados em pt-BR (`12,00`) após a edição. Teclado decimal, botão Concluído e ajustes para IME.
- A tela principal e os painéis inferiores continuam disponíveis; usuários existentes preservam orçamento e podem abrir o wizard pelo menu `$`.
- Persistência JSON schema 3 com retrocompatibilidade v1 e v2, mesmo pacote e mesmas SharedPreferences.
- Workflow de assinatura opcional mantido. **Não inclui Secrets ou chave privada.**

**Validação:** testes Kotlin/JVM com executor local compatível e análise sintática PSI; compilação Android Gradle e testes físicos de layout/teclado ainda pendentes.
