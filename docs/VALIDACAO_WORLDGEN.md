# Minecraft 2 — implementação e validação de geração

Data: **21/10/2026**. Alvo: Minecraft Java **1.21.1**, NeoForge **21.1.263**.

## Inventário e instalação observada

`modpack/mods.zip` contém **111 JARs**. O perfil local `M2D` acessível durante esta execução tinha os mesmos 111 nomes e os mesmos SHA-256. Isso é uma comparação dos arquivos disponíveis, não prova de que um cliente estava aberto ou usava esse diretório naquele momento. O `minecraftinstance.json` local descreve somente 99 addons; não foi usado como lista definitiva.

- `modpack/mods-list.txt`: lista real do ZIP, com versões declaradas internamente.
- `modpack/mods-manifest.json`: arquivos, hashes SHA-256, mod IDs, loaders, licenças declaradas, dependências, bibliotecas Jar-in-Jar e origens identificáveis nos metadados/launcher.
- `modpack/integration-manifest.json`: os dois JARs adicionais compilados localmente para esta integração. Eles **não estavam no ZIP nem foram instalados na instância ativa**.

Não faltaram IDs de dependências obrigatórias declaradas, considerando bibliotecas embarcadas. Isso não certifica dependências não declaradas ou compatibilidade do conjunto inteiro. IDs repetidos em bibliotecas embarcadas, como `conditional_mixin`, `sablecompanion`, APIs Fabric, `mezz_config` e `betteradvancedtooltips`, não equivalem a dois mods independentes duplicados: o loader faz a seleção JarJar. O servidor chegou a carregar esse conjunto.

Há versões divergentes entre nome e metadata: Ars Fauna `1.4.1` no nome declara `1.3.1`; Ars Structurize `1.1.3` declara `1.0.0`; Macabre `0.9.2` declara `0.9.1`. O manifesto preserva o nome completo e a versão declarada, sem corrigir ou renomear JARs de terceiros.

A checagem Maven estrita encontrou **seis intervalos Minecraft declarados divergentes**: Dungeons Arise, Undergarden, Ars Additions, Iris, Iron Chests e JEI. Esses intervalos ficam registrados no manifesto. O servidor carregou os componentes de servidor apesar dessas declarações; portanto não foram tratados como uma recusa efetiva do loader. Iris pertence ao cliente e não foi validado nesse teste. Intervalos `incompatible` são interpretados como proibições, não como requisitos.

O Git não rastreava JARs. Rastreava uma DLL nativa do Sable e `M2D.zip`; ambos foram retirados somente do índice. A DLL local foi preservada; `M2D.zip` já estava ausente no início da tarefa. O ZIP atual, JARs, caches e outputs de teste estão ignorados. Não houve commit, push ou publicação.

## Geração e restrições implementadas

| Mundo | Geração preservada/adaptada | Largura X/Z | Intervalo de blocos |
|---|---|---:|---|
| Overworld | BYG/TerraBlender e integrações anteriores | 9984 | −4992 a 4991 |
| End | WoverChunkGenerator / BetterEnd | 6016 | −3008 a 3007 |
| Nether | WoverChunkGenerator / BetterNether | 4992 | −2496 a 2495 |
| Pokémon | Terralith, com os 1.713 parâmetros anteriores | 4992 | −2496 a 2495 |
| Undergarden | UGNoiseBasedChunkGenerator | 4992 | −2496 a 2495 |
| Arcanus | Stratospherical 0.4.1 adaptado + floresta de archwood | 3008 | −1504 a 1503 |
| Carne (`macabre:the_pit`) | Noise settings, tipo e biomas originais do Macabre | 2016 | −1008 a 1007 |

O preset é **`m2worlds:toroidal`**, disponível na lista de criação de mundos. Em servidor novo: `level-type=m2worlds\:toroidal`. `force_default_world_preset=false` impede que WorldWeaver substitua a escolha silenciosamente. As quatro dimensões de datapack carregam seus próprios limites; não é necessário duplicar seus parâmetros climáticos no preset.

Os limites são intervalos semiabertos de chunks, com larguras pares, mínimo 16 chunks e End com mínimo 192, conforme o código da versão Exotic Worlds **1.0.0** efetivamente presente no bundle. A escala do tipo de dimensão do Nether foi configurada para **2**, além da razão 624:312 chunks. O tipo original do Undergarden continua com escala 4; sua travessia com jogador ainda exige teste dedicado. Exotic Worlds também atribui automaticamente formas derivadas a outras dimensões, como limbo/distortion; isso não as identifica como Carne. Após a indicação do usuário, `macabre:the_pit` foi confirmado no recurso real `data/macabre/dimension/the_pit.json`, preservado e recebeu limites independentes.

### Stratospherical e TerraBlender

Stratospherical não constava do ZIP. Foi examinado e compilado localmente a partir de [Kamilhik/Stratospherical-Expansion-NeoForge](https://github.com/Kamilhik/Stratospherical-Expansion-NeoForge/tree/e5da51aabda9801885a15a446001e159411a63ea), commit **`e5da51aabda9801885a15a446001e159411a63ea`**, versão upstream **0.4.1**, licença MIT. O fork local é **`0.4.1-m2.1`**. A [licença upstream completa](../integration/third-party/strato-LICENSE.txt) acompanha os recursos e o JAR; copyright original **2022 Apothicon02**.

O upstream substitui `minecraft:overworld`, redefine 48 biomas vanilla, chama `SurfaceRuleManager.setDefaultSurfaceRules(OVERWORLD, ...)` e altera globalmente uma regra de dripstone. O patch reproduzível:

1. Retira os overrides vanilla da cópia local de build e remove a chamada global de superfície.
2. Transfere os 48 biomas e os dois biomas vanilla adicionais referenciados para `m2arcana:strato/*`, preservando listas de features/carvers e o noise router do Strato.
3. Mantém o conjunto de 7.590 entradas climáticas. Em 64 entradas de floresta temperada úmida, usa a floresta de archwood já existente, conservando a integração Ars.
4. Remapeia testes de bioma nas surface rules. Não substitui os noise settings do Overworld.
5. Isola dependências vanilla de terreno e as features/carvers que colidem com overrides do Terralith. Os recursos locais ficam sob `m2arcana:vanilla/*`; IDs de blocos e tipos de codec permanecem intactos.
6. Transfere a ampliação de pedra-base para `strato:arcanus_base_stone` e a exceção de gelo para o helper próprio do Strato. O mixin vanilla global deixa de ser carregado.
7. Bloqueia features/estruturas/carvers `strato` nos demais conjuntos pelo Router.

A primeira inspeção de runtime revelou que MultiNoiseBiomeSource recebia regiões globais do TerraBlender também nos mundos customizados. A integração Java registra **`m2_worldgen:isolated_climate`**, que usa a mesma busca climática vanilla e os parâmetros existentes, sem herdar MultiNoiseBiomeSource. Isso preserva a paleta BYG no Overworld e impede a injeção dessas regiões nas duas dimensões isoladas.

O Overworld conserva os demais mods e recursos já presentes, inclusive os overrides vanilla do Terralith que já estavam instalados. BYG não implementa uma classe exclusiva de chunk generator: sua integração principal ocorre por regiões e superfície do TerraBlender. Não se deve interpretar a tabela como remoção dos demais conteúdos anteriores do Overworld.

### Cobblemon, Alex's Mobs e addons Ars

- O bloqueio existente `config/cobblemon/main.json` e os spawn pools/estruturas/tags Pokémon foram preservados. O mixin adicional restringe `CobblemonBiomeModifiers.modify` aos clones `m2pokemon`, incluindo as entradas codificadas que não exigem tags. O Router complementa a restrição de features/estruturas fora do Pokémon.
- Alex's Mobs usa filtro de spawn natural e de geração de chunks do Router, mais proteção em `SpawnPlacements`/`FinalizeSpawnEvent`. O spawner ambiental de baleias recebe uma proteção própria porque usa `SPAWNER` internamente. Não há um bloqueio geral de `SPAWNER`, ovos, comandos ou invocações.
- Ars Additions: namespace `ars_additions`, **4 estruturas** adaptadas individualmente.
- Ars Structurize: namespace `ars_structurize`, **8 estruturas** adaptadas individualmente.
- Ars Fauna: namespace `ars_fauna`, **25 biome modifiers** adaptados individualmente. Alguns predicados chamam `EntityType.spawn(...MOB_SUMMONED)` durante a checagem natural; a integração interrompe a checagem antes desses efeitos fora de Arcanus.
- Os seletores são derivados dos recursos e tags reais. `modpack/worldgen-selectors.json` documenta quantos biomas correspondem a cada recurso. Restaram dois seletores vazios: Enderbuncle e Sculkbuncle, cujos habitats não existem na paleta Strato. Esses habitats não foram inventados nem seus predicados removidos para forçar spawns. Predicados ambientais dos demais mobs também continuam ativos.
- Itens, receitas, magias, drops e inventário não recebem políticas de proibição. As categorias `item` e `biome` do Router ficam desativadas; não há substituição por biomas fallback.
- Features, spawns e estruturas Ars Nouveau/Ars Elemental anteriores permanecem nos clones e seletores existentes de Arcanus.

## Reprodução

Os comandos abaixo partem da raiz do repositório. Exigem Python 3.11+, JDK 21, Git, o ZIP local e as bibliotecas de servidor NeoForge 21.1.263 obtidas pelo instalador oficial em `modpack/.cache/server`. `VANILLA_EXTRA` é o JAR de dados `server-1.21.1-20240808.144430-extra.jar`; `CLIENT_SRG` é o JAR Mojmap de cliente da mesma versão. Não use JARs obfuscados `slim`/`unpacked` no classpath.

```powershell
python tools/audit_mods.py --active 'DIRETORIO_MODS_DO_PERFIL' --jdk 'JDK_21'
git clone https://github.com/Kamilhik/Stratospherical-Expansion-NeoForge modpack/.cache/strato-source
git -C modpack/.cache/strato-source checkout e5da51aabda9801885a15a446001e159411a63ea
python tools/prepare_strato.py --source modpack/.cache/strato-source --vanilla 'VANILLA_EXTRA'
python tools/build_strato.py --jdk 'JDK_21' --client-jar 'CLIENT_SRG'
python tools/prepare_strato.py --source modpack/.cache/strato-source --apply
python tools/build_integration.py --jdk 'JDK_21'
python tools/generate_worldgen.py --vanilla 'VANILLA_EXTRA'
python tools/stage_worldgen_profile.py
```

O staging fica em **`modpack/.cache/worldgen-overrides`**. Ele contém configs, defaultconfigs, KubeJS e somente os dois JARs adicionais. Copie-o para **uma cópia de teste do perfil com os mods auditados**, faça reinício completo e crie um mundo novo com o preset acima. Não instale somente os JSONs: eles dependem do codec Java e do fork Strato. Não use o JAR upstream sem patch junto deles.

O build por javac usa as bibliotecas locais e timestamps fixos no JAR. A tentativa com Gradle upstream demorou na resolução de dependências; a compilação entregue foi feita pelo script local, sem modificar metadados dos 111 JARs originais. O site foi validado após instalar suas dependências sem gerar lockfile.

## Validação executada e limites

Consulte **`docs/worldgen-runtime-validation.json`** para a execução final do mod de teste em servidor dedicado local, com Java **21.0.4**, mundo novo e seed **420211**. O teste é separado da integração normal e encerra o servidor ao terminar.

Foram executados: compilação Java 21, carregamento do servidor com as integrações, validação do Router, inspeção das paletas, geração de chunks nas bordas, checagem dos limites e da escala, inspeção da injeção de features Cobblemon e testes de spawns com motivos específicos. O teste também escreve os codecs efetivamente carregados para arquivos locais de auditoria. Os resultados e eventuais falhas ficam no JSON; não são substituídos por uma afirmação de compatibilidade geral.

O resultado final foi **`passed: true`**, sem erros no teste das **sete dimensões**, no mundo novo `m2-portals-validation-v3`. Foram observados 54 biomas BYG na paleta do Overworld, 148 clones no Pokémon, 51 no Arcanus e 15 biomas Macabre em The Pit. As 1.558 injeções de features Cobblemon encontradas ficaram somente em biomas `m2pokemon`. O teste confirmou a mesma instância de chunk nas coordenadas equivalentes das bordas X/Z e sete verificações internas de periodicidade do Exotic Worlds, com nove amostras cada no eixo X.

Os testes de spawn efetivo usaram `alexsmobs:grizzly_bear` e `ars_fauna:chefbuncle` com motivos `NATURAL`, `CHUNK_GENERATION` e `COMMAND`, verificando `isAddedToLevel()`. As proibições naturais e a permissão de comando coincidiram com a política nas sete dimensões. A regra é por namespace; isso não equivale a observar estatisticamente cada espécie do mod. Sem jogadores, o lookup de UUID pode ocultar entidades em seções não rastreadas, por isso ele não foi usado como prova de inserção.

Verificações estáticas: **702 JSONs e 3 TOMLs** válidos no conjunto de dados e integrações examinado, `node --check` no script novo, identidade dos 1.713 parâmetros e noise settings anteriores do Pokémon, identidade dos dados originais do The Pit após retirar somente os campos toroidais, ausência de overrides vanilla e da chamada global de superfície no fork Strato, verificação do Git e `git diff --check`. O próprio KubeJS carregou o script de portais e o BBL registrou os três materiais com os validadores corretos. **`cd site; npm run build` passou**, gerando quatro páginas, sem alterações no código do site ou no prefixo GitHub Pages. Uma recompilação do JAR principal reproduziu o SHA-256 **`619e133327b4721864e17a6601c75b92cc26369fc058b977c413e3687f765f99`**.

Os portais passaram em **52 casos de moldura/ignição**, incluindo os dois eixos, limites de tamanho, cantos ausentes, lateral incompleta, metades invertidas, mistura indevida, clique embaixo/em cima, Catalyst e Flint and Steel. `FakePlayer` publicou eventos reais; o Catalyst manteve quantidade 1 e dano 0. Entidades atravessaram os dois pares BBL e retornaram ao portal original, com cooldown, molduras corretas no destino, plataformas e vínculos preservados na serialização NBT. O Catalyst original acendeu o Undergarden; Flint and Steel acendeu o Nether. Uma entidade viajou pelo NetherPortalBlock vanilla: X=4000,5 no Overworld → X=1988,5 no Nether, dentro da tolerância de busca de portal ao redor do alvo escalado X=2000,25. Isso confirma a escala 1:2 no transporte testado, não a correspondência exata entre centros de molduras.

Para registrar novamente somente uma execução concluída e aprovada, use `python tools/record_worldgen_validation.py --log NOME_DO_LOG.txt`. O script compara o resultado final do log com o JSON de auditoria e os hashes dos JARs testados com o manifesto; recusa resultados antigos, parciais ou com erros.

Ainda dependem de validação com cliente e jogador: cores dos portais, duas conexões multiplayer, saída/retorno após reiniciar o servidor, travessia visual dos quatro lados/cantos, ida/volta pelos portais Nether perto das bordas, conversão de coordenadas de jogador, travessia do portal Undergarden, seguimento de mobs, estruturas que cruzam costuras, amostragem extensa de vegetação e balanceamento dos spawns. O fold, as amostras e as viagens de entidades não certificam todos esses comportamentos. Não foi aberto um cliente Minecraft nesta execução. Não foi convertido nenhum save existente para toroidal.

O conjunto original também emitiu erros de loot tables opcionais do Railways, receitas Create/Galosphere, advancements Dungeons Arise, metadata de packs e avisos de categoria de spawn do Quark. Na geração houve também aviso Lithostitched de template pool `minecraft:empt` e aviso de faixa de altura vazia. Não há referência a `minecraft:empt` nos JSONs próprios; o aviso vem dos recursos de geração carregados. Esses avisos continuam como limitações de recursos de terceiros a investigar, sem excluir estruturas ou mods para escondê-los. Os testes de servidor omitem mods exclusivamente de cliente; o conjunto de 111 JARs sem essa separação falhou primeiro no prelaunch do Sodium por ausência de LWJGL no servidor dedicado.

### Portais e preservação

A continuação da solicitação foi recebida e implementada em [PORTAIS_DIMENSIONAIS.md](PORTAIS_DIMENSIONAIS.md): BBL Portals + KubeJS, Catalyst original compartilhado, molduras posicionais e complemento Java reproduzível. As funções `/trigger`, configuração de spawn Cobblemon, integração Ars Nouveau/Elemental, receitas/loot/progressão, portal Undergarden e portal vanilla Nether foram preservados. Não foram instalados todos os mods auxiliares em sistemas novos: History Stages e LootJS permanecem sem alterações; ProbeJS não foi necessário porque a API foi examinada diretamente no JAR e compilada/testada.

## Estado final e arquivos

| Tarefa | Implementação e evidência | Validação restante |
|---|---|---|
| Gitignore/inventário | Ignorar binários; retirar somente do índice; 111 JARs auditados e comparados com o perfil; manifesto com SHA-256 | Distribuição/licenças não aprovada em conjunto |
| BYG / Terralith / Strato | Preservação de geradores, isolamento de clima e superfície; paletas inspecionadas no servidor | Exploração visual e amostragem extensa |
| Cobblemon | Injetor Java + recursos existentes + Router; 1.558 features somente nos clones Pokémon | Inspeção estatística de estruturas/vegetação e spawns selvagens |
| Alex's Mobs | Natural/chunks bloqueados nos dois mundos, proteção especial de baleias | Observar cada espécie e spawner ambiental |
| Addons Ars | 37 recursos adaptados + filtros naturais; comandos permitidos | Balanceamento; dois habitats Fauna ausentes continuam sem geração |
| Exotic Worlds | Sete larguras independentes, chunks dobrados, periodicidade, escala 1:2 no transporte Nether | Costuras, estruturas/mobs e portais perto das bordas com jogador |
| Portais BBL | Catalyst, molduras, ida/volta, cooldown, links serializados e portais originais testados no servidor | Cliente, multiplayer e retorno após reinício |
| Reprodutibilidade | Fontes Java/Python, fonte Strato fixada, build determinístico, staging local | Instalar staging em cópia de perfil cliente e executar checklist |

Arquivos modificados: `.gitignore`, README/documentação de mods, lista de mods, `wover/main.json`, JSONs de dimensão Pokémon/Arcanus e tag de biomas Arcanus. Arquivos criados: manifestos, regras Router, preset/tipo Nether, limites Undergarden/The Pit, recursos isolados Strato/addons, script KubeJS de portais, fontes/testes Java, ferramentas Python e relatórios. As duas remoções do índice são `M2D.zip` (já ausente localmente) e DLL nativa Sable (preservada localmente). Nenhum JAR foi adicionado ao índice.

Preservados: funções e comandos administrativos, scripts server/client, configurações Cobblemon existentes, código do site, as integrações Pokémon/Arcanus anteriores necessárias, itens/receitas/equipamentos/progressão/loot e todos os arquivos da instalação ativa. Os novos JARs ficam somente no build/staging/teste local, ignorados pelo Git. Nenhum commit, push ou publicação foi realizado.

Fontes técnicas: [documentação oficial do Exotic Worlds para servidor](https://github.com/anyttng/exotic_worlds/blob/mc/1.21/docs/dedicated-server.md), [API oficial](https://github.com/anyttng/exotic_worlds/blob/mc/1.21/docs/modding.md), fonte Strato fixado acima e inspeção dos JARs exatos do manifesto. O repositório indicado no metadata do Worldgen Router retornou indisponível; seu schema e seus pontos de filtragem foram verificados no bytecode de **1.0.0** local.
