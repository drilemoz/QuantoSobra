# Quanto Sobra? — 0.1.0-alpha.6

**31/10/2026 · versionCode 6**

- Suporte à assinatura Release estável por quatro GitHub Secrets.
- Workflow gera Debug sem Secrets; gera Release assinado somente quando todos os Secrets estiverem definidos.
- Certificado e senhas ficam fora do ZIP de código-fonte e fora do repositório.
- Nenhuma alteração na UI, no esquema de dados nem nos cálculos da alpha.5.

**Atenção:** o APK Release com a nova chave não atualiza APKs Debug já instalados. Backup/restauração ainda não implementados; preserve os dados. Build e APK Release ainda precisam de validação no GitHub Actions.
