# Minecraft 2 — sincronização M2D e Undergarden, 01/11/2026

## Estado da entrega

**Implementado e instalado no perfil real; gameplay não testado.** O usuário pediu execução sem perguntas, sem testes e sem ações que dependessem de autorização adicional. Nenhum Minecraft, servidor de teste, suíte de validação ou build do site foi iniciado. A compilação de produção foi necessária para gerar o JAR que instala o serializer das mochilas e os eventos de água. Não equivale a teste de gameplay.

Base local e main remota inspecionada: `d044aa52031a8513403676a254aa2cf73316eb89`. Não foram incorporadas branches experimentais. A pasta real foi confirmada pelo `minecraftinstance.json`: M2D, Minecraft 1.21.1, NeoForge 21.1.263. Nenhum processo java/javaw ativo nas duas consultas anteriores a cada cópia. A consulta CIM indisponível foi substituída por `Get-Process`, sem elevação. Saves não foram abertos, alterados, regenerados ou convertidos.

## A. Sincronização e inventários

**148 arquivos .jar ativos, zero desativados; 146 externos e duas integrações** (`m2_worldgen` e fork Strato). “Ativo” indica extensão carregável na pasta mods, não sucesso de boot. O fork Strato deriva de terceiro sob licença MIT; não é confundido com mod externo upstream ou planejamento.

Inventário anterior: **122 JARs**. Agora: **43 nomes de arquivo adicionados e 17 retirados/substituídos no inventário**, nenhum hash diferente entre nomes comuns, nenhum ID principal removido e 16 IDs com versão alterada. Isso é comparação de inventários; nenhum mod externo foi instalado, removido ou atualizado por esta tarefa. A instância já tinha essas mudanças.

O ZIP histórico continua com 134 JARs, SHA-256 `f9e5866f8b2b553369edaca9c9232e346c0c76dcae77c069c66f692de19f9bbe`; sua auditoria separada não é a fonte do inventário ativo. Ele mantém a duplicata Iris e não contém Strato. O perfil real tem somente Iris `1.8.14-beta.1+mc1.21.1`, fork `strato-0.4.1-m2.1.jar` e nenhum JAR exclusivo de testes.

Metadados lidos diretamente dos JARs e JarJar: nome/ID/versão, loader, dependências obrigatórias e opcionais, intervalos Minecraft, licença declarada e SHA-256. Nenhuma dependência obrigatória identificada ausente, nenhum ID principal duplicado e nenhum JAR principal idêntico por hash. Bibliotecas JarJar repetidas estão discriminadas, sem tratá-las automaticamente como duplicação inválida no loader.

Persistem **sete divergências declaradas de Minecraft**: Ars Additions, Dungeons Arise, Iris, Iron Chests, JEI e More Culling excluem 1.21.1 pelo limite superior aberto; Undergarden declara `[1.21]`. Nada foi remendado nos JARs. Metadados e licenças não certificam compatibilidade nem autorização de distribuição. Intervalos completos: [intervalos-perfil-20261101.json](intervalos-perfil-20261101.json).

### Personalizações estáveis da main

A comparação leu 1.271 arquivos rastreados de config/defaultconfigs/KubeJS: **1.257 idênticos**, **dez divergências locais preservadas** e **quatro ausências de índice/cache Ars não instaladas**. Configs de worldgen, presets, dimensões, portais, restrições de pragas, Cataclysm e scripts anteriores já coincidiam. Fork Strato instalado coincide com o SHA histórico `865451f443b83ea80e9cc5e010ac75468368610c5d75cbe1fe25a539cb4d8fa7`; não precisou recompilação ou troca.

Ars Nouveau e Elemental diferem somente pela remoção de comentários, mantendo `archwoodForest=0`/`extra_biomes=0`. Cobblemon mantém `experienceMultiplier=2.0`, blocklist e demais personalizações; o perfil tem lastSavedVersion 1.8.3 e novo `predationChance=0.05`, preservados. O JAR efetivamente instalado é 1.8.4: o campo de configuração não é a versão do mod. Demais divergências são Attributefix knockback resistance, Iris, Jade (dois arquivos), ordens do JEI (dois) e Sodium; nenhuma necessária para as três mecânicas. Não houve substituição de preferências locais.

O contrato Java antigo exigia Exotic Worlds 1.1.4, mas o perfil já tem **1.2.5**. A integração foi compilada com o engine JarJar 1.2.5, preservando as fontes de worldgen/portais, e o requisito foi ajustado para essa versão. Sophisticated Backpacks/Core declaram versões **3.26.19/1.5.23**; os sufixos .2243/.2446 só aparecem nos nomes de arquivo e não foram usados como versões FML.

### IDs adicionados ao inventário

`alltheleaks`, `betterendisland`, `blockpops`, `chunksfadein`, `clumps`, `controlling`, `creativecore`, `deathcharm`, `dungeonnowloading`, `endermanoverhaul`, `entity_pin_cushions`, `irisblockcompat`, `kawaiidishes`, `lithium`, `m2_worldgen`, `moreculling`, `movement_enchants`, `mr_spawn_animationscompats`, `netherman`, `potion_blending`, `searchables`, `servercore`, `simplerecall`, `spawnanimations`, `spore`, `the_backwoods`.

### Versões alteradas

| ID | Antes | Agora |
|---|---|---|
| `betterend` | 21.0.38 | 21.0.39 |
| `betternether` | 21.0.29 | 21.0.30 |
| `cobblemon` | 1.8.2+1.21.1 | 1.8.4+1.21.1 |
| `cobblemonpokedexnetwork` | 1.8.2+1.2.1 | 1.8.4+1.3.0 |
| `exotic_worlds_bundle` | 1.1.4 | 1.2.5 |
| `incontrol` | 1.21-10.3.0 | 1.21-10.3.1 |
| `iris` | 1.8.12-snapshot+mc1.21.1-local | 1.8.14-beta.1+mc1.21.1 |
| `macabre` | 0.9.1 | 1.0.0 |
| `modernfix` | 5.27.26+mc1.21.1 | 5.27.27+mc1.21.1 |
| `platform` | 1.4 | 1.4.2 |
| `puzzleslib` | 21.1.65 | 21.1.66 |
| `quark` | 4.1-487 | 4.1-488 |
| `sophisticatedbackpacks` | 3.26.14 | 3.26.19 |
| `sophisticatedcore` | 1.5.17 | 1.5.23 |
| `strato` | 0.4.6 | 0.4.1-m2.1 |
| `wover` | 21.0.29 | 21.0.30 |

O engine JarJar `exotic_worlds` também é 1.2.5; a tabela principal registra o bundle. `mezz_config` teve troca de nome de arquivo sem mudança identificável no campo de versão; ambos os nomes e hashes constam dos inventários.

### Nomes adicionados

- `BetterEnd-21.0.39.jar`
- `BetterNether-21.0.30.jar`
- `BlockPops - NeoForge - 1.21.1-2.0.2.jar`
- `Clumps-neoforge-1.21.1-19.0.0.1.jar`
- `Cobblemon-neoforge-1.8.4+1.21.1.jar`
- `Controlling-neoforge-1.21.1-19.0.5.jar`
- `CreativeCore_NEOFORGE_v2.13.52_mc1.21.1.jar`
- `Platform-neoforge-1.21.1-1.4.2.jar`
- `Quark-4.1-488.jar`
- `Searchables-neoforge-1.21.1-1.0.2.jar`
- `YungsBetterEndIsland-1.21.1-NeoForge-3.1.2.jar`
- `alltheleaks-1.1.14-1.21.1-neoforge.jar`
- `chunksfadein-neoforge-3.0.26-1.21.jar`
- `cpn-neoforge-1.8.4-1.3.0.jar`
- `cultofazazelneoforge-1.1.4.12.jar`
- `deathcharm-1.0.1-1.21.1.jar`
- `dungeonnowloading-neoforge-1.21.1-2.11.6.jar`
- `endermanoverhaul-neoforge-1.21.1-2.0.3.jar`
- `entity_pin_cushions-neoforge-1.21-1.0.jar`
- `exotic_worlds-1.2.5-mc1.21.1.jar`
- `incontrol-1.21-10.3.1.jar`
- `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar`
- `irisblockcompat-1.21.1-1.1.0.jar`
- `kawaiidishes-1.13.jar`
- `lithium-neoforge-0.15.4+mc1.21.1.jar`
- `m2-worldgen-1.1.0.jar`
- `macabre-1.0.0-neoforge-1.21.1.jar`
- `mezz_config-1.21.1-neoforge-0.6.10-standalone.jar`
- `modernfix-neoforge-5.27.27+mc1.21.1.jar`
- `moreculling-neoforge-1.21.1-1.0.10.jar`
- `movement_enchants-1.1.0-neoforge-1.21.1.jar`
- `potion_combiner-1.21.1-Neoforge-1.0.0.jar`
- `puzzleslib-v21.1.66-mc1.21.1+neoforge.jar`
- `servercore-neoforge-1.5.19+1.21.1.jar`
- `simplerecall-1.0.6-1.21.1.jar`
- `sophisticatedbackpacks-1.21.1-3.26.19.2243.jar`
- `sophisticatedcore-1.21.1-1.5.23.2446.jar`
- `spawn-animations-compats-18.0.jar`
- `spawnanimations-v1.11.6-mc1.17-26.3.9-mod.jar`
- `spore_1.21.1_2.2.0j_neo.jar`
- `strato-0.4.1-m2.1.jar`
- `the_backwoods-1.8.0-neoforge-1.21.1.jar`
- `worldweaver-21.0.30.jar`

### Nomes retirados/substituídos na lista

- `BetterEnd-21.0.38.jar`
- `BetterNether-21.0.29.jar`
- `Cobblemon-neoforge-1.8.2+1.21.1.jar`
- `Platform-neoforge-1.21.1-1.4.jar`
- `Quark-4.1-487.jar`
- `cpn-neoforge-1.8.2+1.2.1.jar`
- `exotic_worlds-1.1.4+mc1.21.1.jar`
- `incontrol-1.21-10.3.0.jar`
- `iris-neoforge-1.8.12+mc1.21.1.jar`
- `macabre-0.9.2-neoforge-1.21.1.jar`
- `mezz_config-1.21.1-neoforge-0.6.8-standalone.jar`
- `modernfix-neoforge-5.27.26+mc1.21.1.jar`
- `puzzleslib-v21.1.65-mc1.21.1+neoforge.jar`
- `sophisticatedbackpacks-1.21.1-3.26.14.2222.jar`
- `sophisticatedcore-1.21.1-1.5.17.2426.jar`
- `strato-0.4.6.jar`
- `worldweaver-21.0.29.jar`

## B. Mochilas — M2-031

Todos os itens abaixo têm namespace `sophisticatedbackpacks:`; todos os materiais têm namespace `undergarden:`.

| Etapa/nome inglês | ID/aparência | Exige | Material |
|---|---|---|---|
| Leather Backpack | `backpack` | Receita original | Couro, strings e baú originais |
| Cloggrum Backpack | `netherite_backpack` | `backpack` | 8 × `cloggrum_ingot` |
| Froststeel Backpack | `iron_backpack` | `netherite_backpack` | 8 × `froststeel_ingot` |
| Utherium Backpack | `copper_backpack` | `iron_backpack` | 8 × `utherium_crystal` |
| Regalium Backpack | `gold_backpack` | `copper_backpack` | 4 × `regalium_crystal` |
| Forgotten Backpack | `diamond_backpack` | `gold_backpack` | 4 × `forgotten_ingot` |

Não existem lingotes Utherium/Regalium no JAR instalado: os cristais reais foram usados. Custos de oito: anel completo de material com mochila no centro. Custos de quatro: cruz, material acima/abaixo/esquerda/direita. A mochila anterior é consumida pelo crafting normal.

A inspeção de receitas de todos os JARs ativos encontrou sete receitas para as seis mochilas: Leather mais seis rotas evoluídas, incluindo `iron_backpack_from_copper` e smithing `netherite_backpack`. O script remove **todas as receitas por resultado das cinco mochilas evoluídas antes de adicionar as cinco personalizadas**; por isso inclui as seis antigas identificadas e quaisquer outras receitas de resultado explícito carregadas pelo KubeJS. Leather, tingimento e upgrades internos ficam preservados. A remoção em runtime e sua apresentação no JEI ainda não foram testadas.

O serializer `m2_worldgen:backpack_progression` estende `BackpackUpgradeRecipe` real, reaproveitando suas verificações de linked storage. Mantém todos os componentes e o UUID de conteúdo; usa o contrato nativo de `BackpackWrapper.setSlotNumbers` e trata endpoints sem abrir o inventário compartilhado. A capacidade do resultado é o **máximo entre a capacidade efetiva anterior e o valor configurado do destino**. Isso evita que Netherite→Iron/Copper esconda slots ou upgrades. Configs de capacidade não foram modificadas: Leather 27/1; Copper 45/1; Iron 54/2; Gold 81/3; Diamond 108/5; Netherite 120/7. Com essa ordem visual, Cloggrum já tem 120/7 e as etapas seguintes conservam pelo menos essa capacidade. Não se afirma aumento de slots a cada etapa.

As seis chaves de nome em `pt_br` e `en_us` ficam em `kubejs/assets/sophisticatedbackpacks/lang/`, mecanismo de resource pack do KubeJS instalado. Não substituem o JAR ou arquivos inteiros de outras línguas; outras chaves são mescladas normalmente pelo carregador de idiomas. Nomes personalizados pelo jogador são conservados pelos componentes. A exibição real em JEI, tooltip, inventário e crafting, assim como retenção de conteúdo e multiplayer, permanece **pendente**.

## C. Minérios — M2-032

| Feature | Original | Novo | Veio máximo | Altura preservada |
|---|---|---|---|---|
| `undergarden:utherium_ore` | 3 tentativas/chunk | 4 | 8 | Uniforme do fundo da dimensão até Y=32 |
| `undergarden:regalium_ore` | 3 tentativas/chunk | 4 | 4 | Uniforme Y=0–12 |

**+33,333…% de tentativas por chunk para cada minério.** O número real de blocos depende de terreno, alvos substituíveis, exposição ao ar e bioma; não foi medido. Este é o menor aumento inteiro sobre três tentativas e mantém tamanhos dos veios, um ajuste moderado e fácil de alterar com `--count` e `--report extras/RELATORIO_NOVO.json` no gerador, preservando a auditoria anterior.

Método: dois overrides do mesmo ID de `worldgen/placed_feature`, via datapack KubeJS. Não foram criados biome modifiers nem acrescentadas referências duplicadas a biomas. O JAR não contém biome modifiers para esses recursos: as referências são diretas nos biomas Undergarden. Regalium não foi acrescentado aos biomas profundos onde o original não o usa; Utherium conserva seus biomas originais. Configured features intactas: alvos depthrock/shiverstone (Regalium) e também tremblecrust/dreadrock (Utherium); descarte por exposição 0.0/0.5 preservado. Cristais/growth Utherium não foram alterados.

Não altera o gerador ou o preset toroidal, dimensões, alturas ou o Router. Não injeta minério no Overworld. Afeta **apenas chunks ainda não gerados** do Undergarden; não há retrogen nem acesso a regiões existentes. Mundo novo permite comparar desde a origem; não é necessário converter um save para explorar chunks novos. Referências originais, configured features e valores antes/depois estão nos JSONs da entrega.

## D. Veneno — M2-033

Evento real NeoForge 21.1.263: `PlayerTickEvent.Post`, registrado uma vez na integração. Executa só para `ServerPlayer` vivo, dimensão `undergarden:undergarden` confirmada no JAR/perfil, e `isInWater()`. Esse método detecta contato corporal com água fluida, incluindo corrente e rasa, sem exigir submersão e sem incluir chuva ou lava. Não percorre blocos/chunks.

Ao adicionar o UUID a um conjunto de jogadores em água, aplica `MobEffects.POISON` por **60 ticks, amplificador 0 (Poison I)**. Enquanto o UUID continuar presente, não reaplica. Ao sair completamente da água ou ficar fora da dimensão/vivo, remove o UUID. Login, logout, respawn, clone e troca de dimensão limpam explicitamente o jogador; encerramento do servidor limpa o conjunto. Estado não persistido em saves. O registro Java não se repete com `/reload`.

A integração Java já era necessária para impedir a redução de inventário nas mochilas; usar seus eventos de ciclo de vida evita persistir flags no jogador e permite limpar trocas de dimensão diretamente, sem supor eventos KubeJS inexistentes. Não acrescenta mod ou biblioteca. Regras normais de combinação de efeitos preexistentes continuam: um Poison mais forte/longo não é removido à força para impor 60 ticks. A penalidade de armas ×0.40 e `GameplayRules.java` não foram editadas.

**Resultado de testes de veneno: pendente**, como solicitado; não houve servidor/multiplayer iniciado.

## E. Arquivos, instalação e evidências

Instalados seis destinos no M2D, com hashes iguais às fontes e backup anterior à troca:

- `mods/m2-worldgen-1.1.0.jar`
- `kubejs/server_scripts/backpack_progression.js`
- `kubejs/assets/sophisticatedbackpacks/lang/en_us.json`
- `kubejs/assets/sophisticatedbackpacks/lang/pt_br.json`
- `kubejs/data/undergarden/worldgen/placed_feature/utherium_ore.json`
- `kubejs/data/undergarden/worldgen/placed_feature/regalium_ore.json`

JAR final: `c9513fc44a1a9d3ead00ff88c71c9b537d290dcc02981b36c3609e9e45cef16d`. Compilado com Microsoft OpenJDK 21.0.12.1 / `--release 21`, usando APIs reais. Aviso deprecated preexistente em `PlagueWorldgenPlacementMixin`; sem erro de compilação. Houve duas cópias de produção nesta sessão: a primeira foi seguida da correção dos intervalos das versões FML e da exigência Exotic Worlds, sem iniciar Minecraft entre elas. O relatório de 00:14:05 e o inventário **final** identificam o resultado válido; os relatórios iniciais têm hashes próprios e não aprovam o resultado final.

Backups ZIP locais ignorados em `extras/`, fontes e JAR próprio anterior reabertos e comparados aos originais. Nenhum inclui mod externo, save ou preferências pessoais. Nenhum JAR/build/cache foi enviado ao GitHub. `mods.zip` permanece byte a byte com seu hash original.

Evidências principais:

- [Inventário final do perfil](auditoria-perfil-m2d-final-20261101.json), [ZIP histórico auditado separadamente](auditoria-perfil-m2d-final-20261101-zip.json) e [intervalos Maven](intervalos-perfil-20261101.json).
- [Dados originais de receitas/biomas](dados-originais-undergarden-20261101.json) e [minérios antes/depois](geracao-minerios-20261101.json).
- [Instalação final, baseline e backup](instalacao-undergarden-20261101-001405.json), [primeira instalação](instalacao-undergarden-20261101-001229.json) e inventário inicial da sessão em `auditoria-perfil-m2d-20261101.json`.
- Inventário antigo preservado em `mods-manifest-historico-61c73ceef2da.json`; manifesto de integrações antigo em `integration-manifest-historico-1684c5c2db8d.json`. Demais auditorias anteriores intactas.

Reinício **completo** do cliente e do servidor é necessário. Recursos de idioma exigem cliente com os assets; `/reload` não instala o serializer Java. Nenhuma regressão de gameplay foi aprovada. Os procedimentos futuros J39–J45 estão em [TESTES.md](../TESTES.md), incluindo conteúdo cheio, linked storage, idiomas, crafting/smithing, água/multiplayer e regressões toroidais/portais/pragas/Cataclysm. O site não foi alterado e seu build não foi executado por solicitação do usuário.

Ferramentas de entrega: `audit_profile.py` inventaria o perfil sem sobrescrever auditorias; `undergarden_ores.py` gera somente os dois overrides a partir do JAR; `install_undergarden.py` recusa Java ativo/IDs duplicados/arquivos locais conflitantes e só copia os seis destinos; `build_integration.py --profile` usa as APIs reais. Os consumidores `build_delivery.py` e `validate_repository.py` foram adaptados para separar o manifesto ativo da auditoria do ZIP; não foram executados. `build_delivery.py` continua produzindo uma comparação histórica com o ZIP, que não equivale à composição ativa aprovada. Os JSONs originais históricos não foram reclassificados como evidência atual.

A criação de branch local falhou por permissão de escrita em `.git`. O conector GitHub confirmou acesso de leitura/escrita e a mesma main remota, mas rejeitou a primeira chamada de criação de árvore com `user rejected MCP tool call`. Não foram criados commits, branch ou PR, nem solicitada elevação/confirmação, respeitando a ausência do usuário. Não houve merge, force push ou publicação. Os arquivos estão prontos no working tree local, ainda na main, com as diferenças preservadas e sem commit local. A main remota e as branches experimentais não foram alteradas. A entrega no GitHub fica bloqueada pela autorização do conector; a instalação real no M2D foi concluída independentemente disso.

## Arquivos alterados/criados no repositório

- `ALTERACOES.md`
- `README.md`
- `TESTES.md`
- `extras/SINCRONIZACAO_UNDERGARDEN_20261101.md`
- `extras/auditoria-perfil-m2d-20261101-zip.json`
- `extras/auditoria-perfil-m2d-20261101.json`
- `extras/auditoria-perfil-m2d-final-20261101-zip.json`
- `extras/auditoria-perfil-m2d-final-20261101.json`
- `extras/dados-originais-undergarden-20261101.json`
- `extras/geracao-minerios-20261101.json`
- `extras/instalacao-undergarden-20261101-001229.json`
- `extras/instalacao-undergarden-20261101-001405.json`
- `extras/integration-manifest-historico-1684c5c2db8d.json`
- `extras/intervalos-perfil-20261101.json`
- `extras/mods-manifest-historico-61c73ceef2da.json`
- `modpack/development/audit_profile.py`
- `modpack/development/build_delivery.py`
- `modpack/development/build_integration.py`
- `modpack/development/install_undergarden.py`
- `modpack/development/undergarden_ores.py`
- `modpack/development/validate_repository.py`
- `modpack/integration-manifest.json`
- `modpack/integration/README.md`
- `modpack/integration/src/main/java/br/com/minecraft2/WorldgenIntegration.java`
- `modpack/integration/src/main/java/br/com/minecraft2/gameplay/BackpackProgressionRecipe.java`
- `modpack/integration/src/main/java/br/com/minecraft2/gameplay/UndergardenWater.java`
- `modpack/integration/src/main/resources/META-INF/neoforge.mods.toml`
- `modpack/kubejs/assets/sophisticatedbackpacks/lang/en_us.json`
- `modpack/kubejs/assets/sophisticatedbackpacks/lang/pt_br.json`
- `modpack/kubejs/data/undergarden/worldgen/placed_feature/regalium_ore.json`
- `modpack/kubejs/data/undergarden/worldgen/placed_feature/utherium_ore.json`
- `modpack/kubejs/server_scripts/backpack_progression.js`
- `modpack/mods-list.txt`
- `modpack/mods-manifest.json`
