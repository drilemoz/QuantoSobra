# Quanto Sobra? — 0.1.0-alpha.17

**05/11/2026 · versionCode 17**

- **Falha corrigida:** encerramento do aplicativo ao abrir a tela de novidades após atualização. A versão anterior tinha 4 novidades, mas a tela tentava usar o 4º ícone de uma lista com apenas 3.
- As novidades agora têm seleção defensiva de ícones: itens adicionais usam símbolo substituto, sem acessar índices inexistentes.
- Testes de regressão adicionados para a quarta novidade e quantidades maiores.
- Mantidos pacote `br.com.quantosobra`, schema JSON 9, preferências e dados financeiros locais; não é necessário apagar os dados para atualizar.
- A compilação Android e os testes em aparelho precisam ser confirmados no GitHub Actions e no dispositivo.
