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

O ZIP local atual `modpack/mods.zip` contém **134 JARs**, auditados na compilação de 23/10/2026 em [extras/compilacao-integracoes-20261023.json](extras/compilacao-integracoes-20261023.json). Há duas versões Iris com o mesmo ID e oito intervalos Minecraft declarados divergentes. A auditoria anterior de **122 JARs**, de 21/10/2026, permanece preservada em [mods-list.txt](modpack/mods-list.txt) e [mods-manifest.json](modpack/mods-manifest.json); seus resultados não aprovam o ZIP novo. O launcher preservado localmente descreve 99 addons e está desatualizado. Não equivale ao inventário. Integrações próprias ficam separadas em [integration-manifest.json](modpack/integration-manifest.json); candidatos continuam como planejamento em [extras/MODS.md](extras/MODS.md).

Na auditoria histórica de 122 JARs, em relação à anterior: 16 nomes adicionados, 5 retirados/substituídos; nenhum hash mudou entre os nomes comuns. Há zero IDs duplicados no nível principal, zero JARs idênticos e nenhuma dependência obrigatória identificada ausente. Bibliotecas JarJar repetidas são discriminadas no manifesto. Persistem seis divergências de intervalos declarados; isso não certifica compatibilidade ou permissão de redistribuição.

Implementados: dimensões Pokémon/Terralith e Arcana/Strato isoladas, limites toroidais, restrições de geração/spawn, portais físicos BBL com Catalyst, desbloqueios administrativos, penalidade de armas no Undergarden, seleção de estruturas WDA e isolamento do TFTH. Consulte o estado individual e as limitações em [ALTERACOES.md](ALTERACOES.md). Os capítulos de progressão, novas receitas e balanceamentos mencionados nas propostas não são funcionalidades implementadas.

**Atenção ao conjunto atual:** a auditoria de 24/10/2026 do ZIP com SHA-256 `f9e5866f8b2b553369edaca9c9232e346c0c76dcae77c069c66f692de19f9bbe` não encontrou o ID `strato` entre seus 134 JARs. A descrição anterior de upstream `0.4.6` nesse ZIP estava incorreta. Arcanus depende do fork isolado `0.4.1-m2.1`; caso exista outro Strato no perfil cliente, substitua-o somente na cópia de teste e nunca instale os dois juntos. Há duas versões Iris no ZIP. Nenhum JAR do ZIP foi removido ou substituído. Os testes históricos com Exotic Worlds `1.0.0` não aprovam `1.1.4`; os testes novos têm seus próprios hashes no relatório de correção.

Para reproduzir os dois JARs e a entrega local no Windows, use Python 3.11+, Git e as bibliotecas Minecraft 1.21.1 / NeoForge 21.1.263 do CurseForge. O build detecta o JDK 21+ real e não altera M2D ou saves. Consulte [a análise e os hashes reproduzidos](extras/COMPILACAO_INTEGRACOES.md). Ferramentas históricas de auditoria/servidor podem exigir bibliotecas em `modpack/.cache/server/libraries`:

```powershell
python modpack/development/audit_mods.py --jdk 'CAMINHO_JDK_21'
python modpack/development/gameplay_data.py --check
python modpack/development/validate_repository.py
python modpack/development/build_delivery.py
python modpack/development/build_integration.py --jdk 'CAMINHO_JDK_21' --tests
```

Saída própria: `modpack/integration/build/m2-worldgen-1.1.0.jar`. O JAR de testes encerra o servidor: use-o somente em perfil descartável. Reinicie completamente após alterações Java ou de startup KubeJS. Os recursos em `modpack/kubejs/data/` funcionam como datapack via KubeJS; não há uma segunda cópia em `overrides/`. Copie configs/defaultconfigs/KubeJS e as integrações necessárias para uma cópia de teste, nunca para um save único. O preset existente é `m2worlds:toroidal`; os detalhes estão no relatório histórico.

Backups próprios foram compactados, reabertos e comparados byte a byte, diretamente em `extras/`. Preferências e metadados pessoais permanecem locais e ignorados. O motor `modpack/showdown/`, extraído/atualizado pelo Cobblemon, permanece local e saiu do versionamento. Caches, mundos e builds são ignorados. A reorganização é registrada em [extras/reorganizacao.json](extras/reorganizacao.json).

## Documentação

Correção de 25/10/2026 — M2-027: os presets **Normal** do Minecraft e do Wover agora incluem os mesmos geradores toroidais de `m2worlds:toroidal`, sem exigir escolher manualmente esse preset especial. Os dois JSONs também foram instalados no perfil local M2D, com backup e conferência de hashes. Reinicie completamente e crie **mundo novo**; saves anteriores permanecem intactos. Procedimentos e evidências em [TESTES.md](TESTES.md).

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

### Correção de worldgen — 24/10/2026

A [auditoria toroidal e de isolamento Ars](extras/CORRECAO_TOROIDAL_20261024.md) distingue os geradores gravados no save local dos resultados em mundo novo com `m2worlds:toroidal`. Copiar configs não converte um save já criado com geradores sem wrapping. As sete larguras são preservadas; não há novo teleporte entre bordas ou world border. O diagnóstico `[M2 SHAPE]` registra divergências no boot. Ars mantém seus recursos na Arcanus e seus itens/magias utilizáveis nas demais dimensões.

A entrega corrigida inclui `config`, `defaultconfigs`, `kubejs`, os dois JARs personalizados e hashes; instalação somente em cópia descartável do perfil, com substituição única do Strato upstream, sem instalar o JAR separado de testes. Consulte o relatório para o caminho final, resultados espaciais e limitações ainda pendentes.

Resultados desta correção: identidade de chunks/blocos e reinício aprovados nas sete dimensões; isolamento Ars inspecionado no servidor; regressão com 170 verificações de gameplay e 52 casos de moldura/ignição. A diversidade Arcanus continua parcial: 19 dos 51 IDs observados no atlas denso de uma seed/Y; archwood, montanhas e oceanos ausentes nesse plano. Cliente e gameplay das costuras permanecem pendentes. O pacote local está em `modpack/integration/build/minecraft2-correcao-toroidal-20261024.zip`, com guia de instalação e relatórios.
