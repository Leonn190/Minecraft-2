# Integração Java do Minecraft 2

Alvo: **Minecraft 1.21.1 / NeoForge 21.1.263 / Java 21**.

O JAR `m2-worldgen-1.1.0.jar` é obrigatório junto dos JSONs novos. Ele registra a fonte `m2_worldgen:isolated_climate`, com a seleção climática original dos mundos Pokémon/Arcanus, e impede que as regiões globais do TerraBlender sejam acrescentadas a essas duas paletas. Os 1.713 parâmetros do Pokémon foram preservados.

Também restringe o injetor de features do Cobblemon aos biomas clonados `m2pokemon`, interrompe spawns `NATURAL`/`CHUNK_GENERATION` de Alex's Mobs e dos três addons nos mundos proibidos e protege contra efeitos colaterais dos predicados do Ars Fauna. O spawner ambiental de baleias do Alex's Mobs recebe uma proteção específica: ele usa `SPAWNER` internamente. Ovos, comandos, equipamentos e invocações do jogador não recebem uma proibição geral.

As classes externas opcionais são mixins `@Pseudo`. O carregamento é fail-fast quando um alvo presente muda de assinatura. Reinicie completamente o jogo após trocar os JARs.

A mesma integração complementa os portais registrados pelo KubeJS com validadores posicionais de moldura e criação correta do destino. Exige **BBL Portals 1.2.2**. Leia [construção, API e testes dos portais](../../extras/PORTAIS_DIMENSIONAIS.md). Não altera Catalyst, portal Undergarden ou portal vanilla Nether.

## Compilação

Na raiz do repositório, compile e prepare a entrega dos dois JARs:

```powershell
python modpack/development/build_delivery.py
```

O comando detecta um JDK real 21+, seleciona somente bibliotecas de Minecraft 1.21.1 / NeoForge 21.1.263 do CurseForge, confere SHA-1 quando declarado e usa classes Mojmap transformadas antes das vanilla. Dependências ausentes com URL/hash declarados vão somente ao cache do projeto. Não mistura versões da árvore global de bibliotecas. As APIs de BBL, KubeJS, Rhino, Exotic Worlds, Router e TerraBlender são extraídas do ZIP local. Não depende de Gradle ou de uma instalação de servidor adicional.

Obtém o fonte Strato no commit fixado, exige checkout limpo e prepara uma cópia nova com o patch existente. Não executa `--apply` nem regenera worldgen. Compila com `--release 21`, classes novas e timestamps fixos, verifica metadados, licença, bytecode, ausência dos overrides/mixin globais e conflitos. A pasta nova `modpack/integration/build/entrega-*` contém os dois JARs, licença, instruções, SHA-256 e auditorias. Tudo permanece local e ignorado pelo Git.

Opções: `--jdk CAMINHO_JDK`, `--install CAMINHO_CURSEFORGE_INSTALL`, `--source CHECKOUT_LIMPO_NO_COMMIT_FIXADO`. Para compilar apenas a integração, use `python modpack/development/build_integration.py`; a opção anterior `--libraries` continua disponível para bibliotecas de servidor. Para o resultado de 23/10/2026, consulte [a análise de compilação](../../extras/COMPILACAO_INTEGRACOES.md). O ZIP atual contém duplicata Iris além do Strato upstream: a entrega registra o bloqueio de instalação sem alterar o ZIP ou M2D.

## Testes no servidor isolado

```powershell
python modpack/development/build_integration.py --jdk 'CAMINHO_DO_JDK_21' --tests
```

O JAR separado `m2-worldgen-tests-1.1.0.jar` pertence **somente ao servidor descartável**: testa as dimensões e encerra o servidor. Nunca coloque esse JAR na instância do jogador. Com `-Dm2.audit.chunks=true`, também gera chunks nas bordas X/Z. A saída é `m2-worldgen-audit.json` no diretório de execução.

Leia [o relatório completo](../../extras/VALIDACAO_WORLDGEN.md) para os resultados, a compilação do Stratospherical isolado e as limitações.

## Gameplay 1.1.0

A integração também contém penalidade de armas no Undergarden e filtros TFTH. Leia [ALTERACOES.md](../../ALTERACOES.md) e [TESTES.md](../../TESTES.md) para cobertura, limitações e evidências atuais. A suíte gameplay pode ser executada separadamente com `-Dm2.audit.gameplayOnly=true`. O ZIP novo contém Strato 0.4.6; não instalar junto com o fork 0.4.1-m2.1. O staging agora recusa essa combinação antes de copiar arquivos. Os resultados de servidor usam a base histórica especificada no relatório, não os 122 JARs novos.
