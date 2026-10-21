# Integração Java do Minecraft 2

Alvo: **Minecraft 1.21.1 / NeoForge 21.1.263 / Java 21**.

O JAR `m2-worldgen-1.0.0.jar` é obrigatório junto dos JSONs novos. Ele registra a fonte `m2_worldgen:isolated_climate`, com a seleção climática original dos mundos Pokémon/Arcanus, e impede que as regiões globais do TerraBlender sejam acrescentadas a essas duas paletas. Os 1.713 parâmetros do Pokémon foram preservados.

Também restringe o injetor de features do Cobblemon aos biomas clonados `m2pokemon`, interrompe spawns `NATURAL`/`CHUNK_GENERATION` de Alex's Mobs e dos três addons nos mundos proibidos e protege contra efeitos colaterais dos predicados do Ars Fauna. O spawner ambiental de baleias do Alex's Mobs recebe uma proteção específica: ele usa `SPAWNER` internamente. Ovos, comandos, equipamentos e invocações do jogador não recebem uma proibição geral.

As classes externas opcionais são mixins `@Pseudo`. O carregamento é fail-fast quando um alvo presente muda de assinatura. Reinicie completamente o jogo após trocar os JARs.

A mesma integração complementa os portais registrados pelo KubeJS com validadores posicionais de moldura e criação correta do destino. Exige **BBL Portals 1.2.2**. Leia [construção, API e testes dos portais](../docs/PORTAIS_DIMENSIONAIS.md). Não altera Catalyst, portal Undergarden ou portal vanilla Nether.

## Compilação

Instale um servidor NeoForge **21.1.263** em `modpack/.cache/server`, usando o instalador oficial, para obter suas bibliotecas Mojmap. Depois, na raiz do repositório:

```powershell
python tools/build_integration.py --jdk 'CAMINHO_DO_JDK_21'
```

Saída local: `integration/build/m2-worldgen-1.0.0.jar`. Não adicionar ao Git. O compilador usa `--release 21`, sem processadores de anotação, e o JAR usa timestamps fixos. As APIs de BBL, KubeJS, Rhino, Exotic Worlds e Router são lidas diretamente do ZIP local auditado; o script não depende de extração manual.

## Testes no servidor isolado

```powershell
python tools/build_integration.py --jdk 'CAMINHO_DO_JDK_21' --tests
```

O JAR separado `m2-worldgen-tests-1.0.0.jar` pertence **somente ao servidor descartável**: testa as dimensões e encerra o servidor. Nunca coloque esse JAR na instância do jogador. Com `-Dm2.audit.chunks=true`, também gera chunks nas bordas X/Z. A saída é `m2-worldgen-audit.json` no diretório de execução.

Leia [o relatório completo](../docs/VALIDACAO_WORLDGEN.md) para os resultados, a compilação do Stratospherical isolado e as limitações.
