# Validação — 0.1.0-alpha.7 (31/10/2026)

## Escopo

- Partiu da alpha.6 real, manteve ID `br.com.quantosobra`, assinatura opcional via Secrets e dados salvos em `quanto_sobra_v1`.
- Schema 3: campos novos opcionais; a leitura antiga sem esses dados usa aluguel/IPTU mensais, transporte público e modo de salário anterior.
- `wizard_completed` default para `prefs.contains("budget")`, para não interromper atualizações de usuários existentes.
- O wizard não usa `LazyColumn` ou `verticalScroll` e reduz UI quando o teclado está aberto; validar alturas, fontes grandes e teclado no Android real.
- Salários CLT usam cálculo 2026 existente; outros vínculos não herdam automaticamente INSS/IRRF CLT.
- Sem chave privada ou Secret no ZIP.

## Verificações locais

- Domínio compilado com `kotlinc`; **16 de 16 testes** aprovados por executor compatível JUnit (não é Gradle/JUnit real).
- Análise sintática Kotlin por parser PSI: **14 arquivos**, **0 erros**.
- XML, YAML e integridade/CRC do ZIP verificados.

## Pendências

- Ainda não foi executado `:app:assembleDebug`, `:app:assembleRelease` nem teste de interface em celular/emulador. Verificar geração de APK no GitHub Actions.
- Responsividade em celulares pequenos, telas com fontes ampliadas, aparição do teclado, retomada do assistente e fórmulas no aparelho dependem de teste real.
- A simulação salarial não substitui folha de pagamento oficial.
