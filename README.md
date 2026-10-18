# Minecraft 2

> **Status:** planejamento / protótipo inicial. Ainda não há versão pública jogável.

**Minecraft 2** é um projeto de modpack para Minecraft **Java Edition 1.21.1**, com **NeoForge**, configurações e scripts **KubeJS**, progressão própria e documentação interativa em um site **Astro**.

## Repositório

| Pasta | Finalidade |
|---|---|
| `site/` | Site oficial e guia do jogador, publicado no GitHub Pages |
| `modpack/overrides/` | Arquivos personalizados exportáveis do modpack (configs, KubeJS etc.) |
| `docs/` | Planejamento e decisões de design |
| `.github/workflows/` | Deploy automático do site |
| `AGENTS.md` | Orientações para colaboradores e agentes de IA |

> Este repositório **não** deve armazenar JARs de mods de terceiros sem licença/autorização. Para distribuir pelo CurseForge, exporte o perfil de acordo com as regras da plataforma (manifest + overrides).

## Site

```bash
cd site
npm install
npm run dev
```

Abra o endereço local exibido pelo Astro. Para testar o build:

```bash
npm run build
```

A configuração já aponta para `https://leonn190.github.io/Minecraft-2/`. Para publicar, abra **Settings > Pages**, escolha **GitHub Actions** em **Build and deployment > Source** e faça o push do workflow na branch `main`.

## Estado do projeto

- [x] Arquitetura inicial de repositório
- [x] Base do site em Astro
- [x] Workflow de GitHub Pages
- [ ] Revisar e travar lista exata de mods NeoForge 1.21.1
- [ ] Importar as configurações e scripts reais do perfil testado
- [ ] Definir requisitos e objetivos de progressão
- [ ] Testar perfil cliente e servidor
- [ ] Exportar primeira versão pelo CurseForge

## Documentação

- [`docs/VISAO.md`](docs/VISAO.md) — identidade e escopo
- [`docs/PROGRESSAO.md`](docs/PROGRESSAO.md) — proposta inicial de capítulos
- [`docs/MODS.md`](docs/MODS.md) — candidatos e validação
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — próximos marcos
- [`AGENTS.md`](AGENTS.md) — regras para trabalho assistido por IA

## Licenças

Ainda não foi definida uma licença para **o conteúdo próprio** deste repositório. Código, artes e conteúdo de terceiros conservam as licenças de seus respectivos autores. Não pressuma permissão de redistribuição de mods de terceiros.
