# Minecraft 2

> **Status:** protótipo com personalizações implementadas; validação com cliente pendente. Ainda não há versão pública aprovada para distribuição.

**Minecraft 2** é um projeto de modpack para Minecraft **Java Edition 1.21.1**, com **NeoForge**, configurações e scripts **KubeJS**, progressão própria e documentação interativa em um site **Astro**.

## Repositório

| Pasta | Finalidade |
|---|---|
| `site/` | Site oficial e guia do jogador, publicado no GitHub Pages |
| `modpack/` | Perfil: configs, defaultconfigs, KubeJS, inventários, fontes Java em `integration/` e ferramentas necessárias em `development/` |
| `extras/` | Documentos, relatórios, planejamento e backups, diretamente na pasta |
| `.github/workflows/` | Deploy automático do site |
| `AGENTS.md` | Orientações para colaboradores e agentes de IA |
| `ALTERACOES.md` | Registro oficial das personalizações e seus arquivos |
| `TESTES.md` | Checklist de jogo e evidências de validação automatizada |

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

## Modpack e desenvolvimento

O ZIP local atual `modpack/mods.zip` contém **122 JARs**, auditados diretamente em 21/10/2026. A lista exata está em [mods-list.txt](modpack/mods-list.txt); [mods-manifest.json](modpack/mods-manifest.json) registra IDs, nomes, versões internas, loaders, dependências, licenças declaradas e SHA-256. O launcher preservado localmente descreve 99 addons e está desatualizado. Não equivale ao inventário. Integrações próprias ficam separadas em [integration-manifest.json](modpack/integration-manifest.json); candidatos continuam como planejamento em [extras/MODS.md](extras/MODS.md).

Em relação à auditoria anterior: 16 nomes adicionados, 5 retirados/substituídos; nenhum hash mudou entre os nomes comuns. Há zero IDs duplicados no nível principal, zero JARs idênticos e nenhuma dependência obrigatória identificada ausente. Bibliotecas JarJar repetidas são discriminadas no manifesto. Persistem seis divergências de intervalos declarados; isso não certifica compatibilidade ou permissão de redistribuição.

Implementados: dimensões Pokémon/Terralith e Arcana/Strato isoladas, limites toroidais, restrições de geração/spawn, portais físicos BBL com Catalyst, desbloqueios administrativos, penalidade de armas no Undergarden, seleção de estruturas WDA e isolamento do TFTH. Consulte o estado individual e as limitações em [ALTERACOES.md](ALTERACOES.md). Os capítulos de progressão, novas receitas e balanceamentos mencionados nas propostas não são funcionalidades implementadas.

**Atenção ao conjunto atual:** o ZIP agora inclui Stratospherical upstream `0.4.6`; a geração Arcana existente depende do fork isolado `0.4.1-m2.1`. Eles compartilham o ID `strato` e não podem ser instalados juntos. O staging recusa essa combinação. Nenhum JAR do ZIP foi removido, atualizado ou substituído nesta alteração. Os testes históricos com Exotic Worlds `1.0.0` e o fork Strato não aprovam o ZIP novo com Exotic Worlds `1.1.4`.

Ferramentas funcionais exigem Python 3.11+, JDK 21 e bibliotecas locais do NeoForge 21.1.263 em `modpack/.cache/server/libraries`:

```powershell
python modpack/development/audit_mods.py --jdk 'CAMINHO_JDK_21'
python modpack/development/gameplay_data.py --check
python modpack/development/validate_repository.py
python modpack/development/build_integration.py --jdk 'CAMINHO_JDK_21'
python modpack/development/build_integration.py --jdk 'CAMINHO_JDK_21' --tests
```

Saída própria: `modpack/integration/build/m2-worldgen-1.1.0.jar`. O JAR de testes encerra o servidor: use-o somente em perfil descartável. Reinicie completamente após alterações Java ou de startup KubeJS. Os recursos em `modpack/kubejs/data/` funcionam como datapack via KubeJS; não há uma segunda cópia em `overrides/`. Copie configs/defaultconfigs/KubeJS e as integrações necessárias para uma cópia de teste, nunca para um save único. O preset existente é `m2worlds:toroidal`; os detalhes estão no relatório histórico.

Backups próprios foram compactados, reabertos e comparados byte a byte, diretamente em `extras/`. Preferências e metadados pessoais permanecem locais e ignorados. O motor `modpack/showdown/`, extraído/atualizado pelo Cobblemon, permanece local e saiu do versionamento. Caches, mundos e builds são ignorados. A reorganização é registrada em [extras/reorganizacao.json](extras/reorganizacao.json).

## Documentação

- [`ALTERACOES.md`](ALTERACOES.md) — personalizações implementadas e limitações
- [`TESTES.md`](TESTES.md) — checklist e validações reais
- [`extras/VISAO.md`](extras/VISAO.md) — identidade e escopo
- [`extras/PROGRESSAO.md`](extras/PROGRESSAO.md) — proposta inicial de capítulos
- [`extras/MODS.md`](extras/MODS.md) — candidatos e validação
- [`extras/ROADMAP.md`](extras/ROADMAP.md) — planejamento histórico
- [`extras/VALIDACAO_WORLDGEN.md`](extras/VALIDACAO_WORLDGEN.md) — resultados anteriores, vinculados aos hashes antigos
- [`extras/PORTAIS_DIMENSIONAIS.md`](extras/PORTAIS_DIMENSIONAIS.md) — molduras e API dos portais BBL
- [`AGENTS.md`](AGENTS.md) — regras para trabalho assistido por IA

## Licenças

Ainda não foi definida uma licença para **o conteúdo próprio** deste repositório. Código, artes e conteúdo de terceiros conservam as licenças de seus respectivos autores. Não pressuma permissão de redistribuição de mods de terceiros.
