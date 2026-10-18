# Instruções para desenvolvimento — Minecraft 2

## Objetivo

Manter um único repositório para o modpack Minecraft 2 (NeoForge 1.21.1), sua documentação e seu site Astro.

## Regras de segurança de alterações

1. Leia `README.md` e os documentos em `docs/` antes de propor mudanças.
2. Altere apenas os arquivos necessários para a tarefa solicitada. Preserve arquivos e funcionalidades não relacionados.
3. Não substitua o modpack inteiro, não exclua arquivos existentes e não reestruture pastas sem autorização explícita.
4. Evite dependências desnecessárias; prefira solução simples e estática no site.
5. **Não adicione JARs de terceiros**, binários proprietários, segredos, tokens, mundos pessoais, caches, logs ou builds gerados ao Git.
6. Não declare que um mod é compatível ou aprovado para distribuição sem checar versões, loader e licença.
7. Conteúdo marcado como `planejamento` ou `proposta` não pode ser apresentado como recurso implementado.
8. Não altere scripts de KubeJS além do escopo solicitado. Código de `startup_scripts` pode exigir reinício completo.
9. Antes de finalizar, se possível, execute `cd site && npm run build`. Para alterações de modpack, documente procedimento de teste; não afirme ter iniciado Minecraft se não iniciou.
10. Informe resumidamente **quais arquivos mudaram**, por quê, e os testes realmente executados.
11. Não faça push, release, publicação ou commit sem solicitação explícita.

## Organização

- `site/`: Astro, site estático, rotas, estilos e conteúdo da web.
- `modpack/overrides/`: estrutura dos arquivos que serão inseridos no perfil CurseForge.
- `docs/`: planejamento e especificação antes de implementação.
- `.github/workflows/`: CI e deploy do site.

## Convenções

- Idioma das interfaces e documentação: português do Brasil.
- Nome do jogo: **Minecraft 2**.
- Plataforma pretendida: Minecraft Java **1.21.1 / NeoForge**.
- Para site GitHub Pages, mantenha o `base: '/Minecraft-2'` em `site/astro.config.mjs` e respeite o prefixo nos links.
- Scripts novos de KubeJS devem ser pequenos, comentados e divididos por tema; scripts startup/server/client devem ficar nas pastas correspondentes.
