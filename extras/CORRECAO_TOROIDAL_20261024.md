# Minecraft 2 — correção toroidal e isolamento de worldgen

Data: 24/10/2026. Alvo: Minecraft Java 1.21.1 / NeoForge 21.1.263 / Exotic Worlds 1.1.4 / Java 21. Nada instalado na instância ativa; nenhum save pessoal alterado; sem commit/push/publicação.

**Estado final: correções implementadas e armazenamento validado no servidor; aprovação integral de continuidade de gameplay e diversidade dos 51 biomas ainda não obtida.** Os limites abaixo fazem parte da entrega.

## Evidência e causa por dimensão

A ferramenta `inspect_saved_worldgen.py` leu o único `level.dat` encontrado no perfil local M2D, comparou seus bytes antes/depois e exportou somente os campos de geração em [geradores-save-local-20261024.json](geradores-save-local-20261024.json). Não exporta nome, seed ou dados dos jogadores. Não há prova de que esse seja o mesmo save de cada relato manual.

| Dimensão | Largura preservada | Gerador no save local | Causa comprovada ou limite da conclusão |
|---|---:|---|---|
| Overworld | 9984 | `minecraft:noise`, sem wrapping | Esse save não contém configuração toroidal no Overworld; alterar um preset no datapack não regrava WorldGenSettings existentes. BYG não é um chunk generator próprio. |
| Nether | 4992 | `wover:betterx`, sem wrapping | Esse save não contém o stamp de forma. O código atual do EW preserva stamps ao substituir geradores Wover; não há evidência de que ele perdeu um stamp nesse save. |
| End | 6016 | `wover:betterx`, sem wrapping | Mesmo diagnóstico do Nether; não há justificativa para trocar BetterEnd por vanilla. |
| Pokémon | 4992 | `exotic_worlds:toroidal`, wrapping presente | Configuração existe; o relato não foi testado no cliente. Identidade real deve ser medida, e não inferida da paleta Terralith. |
| Arcanus | 3008 | `exotic_worlds:toroidal`, wrapping presente | Repetição visual não era evidência suficiente. O servidor novo verifica a mesma instância de chunk e alterações compartilhadas. |
| Undergarden | 4992 | `undergarden:noise`, `exotic_worlds:wrapping` presente | Não foi reproduzida ausência de forma em mundo novo; causa do relato de cliente permanece não determinada. Preservado UGNoiseBasedChunkGenerator. |
| The Pit | 2016 | `exotic_worlds:toroidal`, wrapping presente | Não foi reproduzida ausência de forma em mundo novo; causa do relato de cliente permanece não determinada. Dados Macabre preservados. |

Não é possível recuperar a forma dos três geradores sem wrapping nesse save apenas copiando configs, e a tarefa proíbe converter/alterar saves existentes. A solução entregue usa mundo novo com `m2worlds:toroidal`, acompanhado de diagnóstico `[M2 SHAPE]` por dimensão no boot. Não foi adicionada world border nem teleporte próprio entre extremidades. O nome do preset escolhido não fica provado pelo nome de um datapack: inspecionamos os geradores efetivamente gravados/carregados.

Outras dimensões existentes: `echoes:timeless`, `macabre:limbo`, `threateningly_mobs:distortion_dimension`. Nenhum limite dessas dimensões foi alterado. O comportamento de formas derivadas que o próprio EW atribui não foi apresentado como política nova do Minecraft 2.

## Exotic Worlds: 1.0.0 versus 1.1.4

Inspeção do JAR real do ZIP e das fontes oficiais, revisão 1.0.0 `a7d5fbbb0c2a6ff47486d7988d5149a7023740a9` e revisão 1.1.4 `cd38b9e217ffed110678a80f4910aba9021d3fa3`. Revisões identificadas pelos commits de versão na linha Minecraft 1.21.1; isso não constitui reprodução criptográfica dos JARs upstream. As fontes foram baixadas apenas no cache, sem integrar código/licença de terceiros ao Git.

- `StampedGeneratorCodec` continua lendo `exotic_worlds:wrapping` em subclasses e transportando `CarriedShape`; os accessors internos mudaram de `toroidal$...` para `exotic_worlds$...`. Os JSONs existentes não ficam inválidos por essa renomeação interna.
- `ClimateScale.CODEC` aceita booleanos nas duas revisões: `false` significa OFF, `true` AUTO. Também aceita objeto `{mode: strong}`. Não foi detectada incompatibilidade dos campos utilizados que justificasse downgrade.
- O gerador toroidal usa `wrapping` sem prefixo; Wover e Undergarden usam o prefixo. As duas formas são intencionais e foram conferidas em runtime.
- Entre as revisões há mudanças de geometria/lattice, posicionamento de estruturas, factories de aleatoriedade e mapas Wover (hex/square, cache/memoização e amostragem). Não são versões intercambiáveis para aprovação histórica. A integração própria usa API pública de forma; o complemento climático novo fica preso à versão 1.1.4 no metadata.
- `WoverChunkGeneratorImplMixin` preserva a forma do gerador substituído. `NoiseBasedChunkGeneratorMixin` envolve geração/carvers/superfície no contexto de forma; Wover possui compatibilidade própria para mapas e biomas. As subclasses reais foram preservadas e verificadas, sem wrapper vanilla que apagasse BetterNether/BetterEnd/Undergarden.
- `ServerChunkCacheMixin` dobra solicitações de chunks, e o acesso de blocos usa coordenadas canônicas. Portanto EW implementa identidade de armazenamento, além de periodicidade de terreno. O teste de nove aliases comprova essa propriedade nas posições executadas, sem prometer todos os casos de gameplay.
- O engine NeoForge embarcado declara Minecraft `[1.21.1]` e NeoForge `[21.1.248,)`; 21.1.263 está nesse intervalo. Aceitação pelo loader e testes de servidor têm alcance próprio; não são certificação do cliente completo.
- O upstream lista integrações de mapas, mas os mods exclusivamente de cliente foram excluídos do teste dedicado. Mapas/minimapas continuam não executados nesta entrega.

Fontes: [codec 1.1.4](https://github.com/anyttng/exotic_worlds/blob/cd38b9e217ffed110678a80f4910aba9021d3fa3/common/src/main/java/com/exoticworlds/engine/gen/StampedGeneratorCodec.java), [comparação das revisões](https://github.com/anyttng/exotic_worlds/compare/a7d5fbbb0c2a6ff47486d7988d5149a7023740a9...cd38b9e217ffed110678a80f4910aba9021d3fa3), [funcionamento](https://github.com/anyttng/exotic_worlds/blob/cd38b9e217ffed110678a80f4910aba9021d3fa3/docs/how-it-works.md), [compatibilidades](https://github.com/anyttng/exotic_worlds/blob/cd38b9e217ffed110678a80f4910aba9021d3fa3/docs/supported-mods.md).

## Isolamento Ars — M2-024

Causa: a região `ArchwoodRegion` é de tipo TerraBlender OVERWORLD e substitui florestas vanilla por `ars_nouveau:archwood_forest`. O peso `archwoodForest=2` ativava essa região global. Ars Elemental também registra suas regiões quando `EXTRA_BIOMES>0`. Os biome modifiers existentes já restringiam várias injeções aos clones, mas não impediam regiões TerraBlender originais contendo suas próprias features. Bloquear somente namespaces de addons não cobria Nouveau/Elemental.

Correções: `archwoodForest=0` e `extra_biomes=0` desativam somente as regiões globais. O intervalo real de `archwoodForest`, conferido por bytecode `defineInRange`, inclui zero. Não se desativa o mod, seus registros ou seus recursos. A Arcanus usa `isolated_climate` e clones diretos, sem depender dessas regiões. Router e `WorldgenIntegration.forbidden` passam a cobrir Nouveau/Elemental somente para geração e spawns NATURAL/CHUNK_GENERATION fora da Arcanus. Itens, magias, receitas, equipamentos e invocações manuais continuam sem proibição nova.

Outra correção: `common_flashing_archwood` era aplicado a `#m2arcana:dimension_biomes`. O recurso original usa `#ars_elemental:flashing_tree_biome`, que contém apenas `ars_nouveau:archwood_forest`. O override agora seleciona só o clone equivalente. A feature rara/mista e as demais vegetações anteriores não foram removidas. Isso elimina uma injeção comum indevida em toda a paleta, sem transformar desertos/planícies em novas florestas mágicas.

Runtime medido: a paleta Overworld preserva BYG e não contém namespaces Ars Nouveau/Elemental; features Ars após biome modifiers: 215 referências em biomas `m2arcana`, zero em `minecraft`/BYG. Há features nos biomas originais registrados dos próprios Ars (63 em `ars_elemental`, 4 em `ars_nouveau`); registro não equivale a geração no Overworld. A operação de chunks usa coordenadas canônicas no servidor; a representação visual/F3 do cliente não é, isoladamente, critério de aprovação ou falha de topologia. Exploração visual e observação de cada criatura continuam pendentes.

## Diversidade Arcanus — M2-025

Fork preservado: upstream Stratospherical 0.4.1 no commit `e5da51aabda9801885a15a446001e159411a63ea`, versão isolada `0.4.1-m2.1`, MIT; não houve alteração nas fontes do fork nesta tarefa. A recompilação reproduz seu hash histórico. A fonte `isolated_climate` conserva a busca vanilla por distância climática e 7.590 entradas/51 IDs; entradas não equivalem a pesos de frequência espacial. Nenhuma faixa/peso/bioma foi inventado ou reescrito.

O noise router Strato preservado usa continentalidade **constante 0.115**, temperatura/umidade com escala 0.115 e dependências de erosão/depth de large biomes. Isso limita a variedade atingível num mapa de 3.008 blocos; a lista de 51 possíveis não comprova a presença de todos, especialmente oceanos. Também havia a injeção comum de flashing em todos os clones, corrigida acima.

Amostragem real: seed 420211, toda a extensão −1504 até 1503, passo 64 em X/Z, 47×47 = 2.209 pontos, no contexto do gerador EW e sampler real do servidor. Y=64 e, nas execuções ampliadas, altura-base calculada pelo gerador antes de features. Isso mede a seleção espacial real do clima, sem confundir com exploração de todos os chunks ou inspeção da vegetação.

| Opção oficial | Biomas distintos observados Y=64 | Resultado |
|---|---:|---|
| OFF, baseline climático após isolamento Ars | 12 | Florestas temperadas, taigas, planícies e rios; archwood/deserto/badlands/montanhas não observados nessa grade |
| AUTO | 16 | Acrescentou archwood, jungle, sparse jungle, bamboo jungle e savanna; manteve ausência de vários ambientes |
| STRONG com aliases climáticos reconhecidos | 19 | Deserto (28 pontos), badlands (112), eroded badlands (53), wooded badlands (8), planícies, florestas, savanas e selvas; mesmas contagens na altura-base |

A opção STRONG oficial usa pelo menos fator 4 apenas em temperatura/vegetação. Uma combinação experimental AUTO + mínimo climático foi também amostrada densamente e produziu exatamente as mesmas contagens (19, sem archwood); [evidência da comparação](atlas-arcanus-comparacao-20261024.json). O complemento experimental foi descartado, não integra a entrega e não ampliou a lista de dependências.  EW reconhecia somente os quatro IDs vanilla; os parâmetros isolados têm nomes `m2arcana:vanilla/temperature*` e `vegetation*`. `ArcanusClimateFieldsMixin` reconhece exatamente esses quatro aliases. Não classifica erosão, continentalidade, densidade ou ruídos de cavernas como clima. A alternativa AUTO também comprime campos horizontais não climáticos; por isso a comparação STRONG é pertinente à preservação do terreno Strato. O resultado final está em [topologia STRONG](topologia-strong-write-20261024.json) e [grade de coordenadas](topologia-strong-write-20261024.csv); [OFF](topologia-off-write-20261024.json) e [AUTO](topologia-auto-write-20261024.json) ficam preservados. Archwood apareceu em dois pontos AUTO, mas não foi observado nos 2.209 pontos STRONG; o ID, suas 64 entradas e suas features foram preservados. O [atlas denso](atlas-arcanus-20261024.json) cobriu todos os **565.504 pontos X/Z de bioma em Y=64** e confirmou 19 biomas, sem archwood, montanhas ou oceanos nesse plano e nessa seed. A presença de archwood em outras seeds/alturas continua não comprovada; sua geração não está aprovada por esta tarefa. A feature rara de archwood nos demais clones permanece, mas árvore isolada não comprova o bioma. Logo a diversidade final e a presença natural de todos os habitats Ars continuam **parcialmente resolvidas**, não aprovadas integralmente. Não se promete tornar todos os 51 IDs presentes sem rever o desenho original do Strato, o que exigiria mudar seu router além desta correção.

Não foram acrescentados os biomas originais de Ars Elemental à paleta: a integração anterior já tinha 50 clones Strato e apenas o clone `ars_nouveau:archwood_forest`. Vegetação/spawns/estruturas Elemental nos clones continuam preservados. Registrar outros biomas do addon não os torna ocorrentes na Arcanus; ampliar essa paleta exigiria outra revisão dos seletores climáticos, sem inventar pesos para esta entrega.

## Matriz de servidor e limites

| Dimensão | Gerador efetivo no servidor final | X/Z | Chunks + leitura/escrita/remoção | Reabertura e 9 marcadores | Cliente/fluídos/entidades/costuras |
|---|---|---:|---|---|---|
| `m2arcana:world` | `LoopedChunkGenerator` | 3008 | aprovado nas 81 comparações | aprovado | não executado |
| `m2pokemon:world` | `LoopedChunkGenerator` | 4992 | aprovado nas 81 comparações | aprovado | não executado |
| `macabre:the_pit` | `LoopedChunkGenerator` | 2016 | aprovado nas 81 comparações | aprovado | não executado |
| `minecraft:overworld` | `LoopedChunkGenerator` | 9984 | aprovado nas 81 comparações | aprovado | não executado |
| `minecraft:the_end` | `WoverChunkGenerator` | 6016 | aprovado nas 81 comparações | aprovado | não executado |
| `minecraft:the_nether` | `WoverChunkGenerator` | 4992 | aprovado nas 81 comparações | aprovado | não executado |
| `undergarden:undergarden` | `UGNoiseBasedChunkGenerator` | 4992 | aprovado nas 81 comparações | aprovado | não executado |

Evidências: [escrita STRONG](topologia-strong-write-20261024.json), [reabertura STRONG](topologia-strong-reopen-20261024.json), [reabertura OFF](topologia-off-reopen-20261024.json). A reabertura final confirmou os 63 marcadores e salvamento completo após `Stopping server`. O primeiro write STRONG foi finalizado após um salvamento completo explícito da suíte, antes de terminar o segundo salvamento do shutdown; a reabertura verificou os blocos persistidos e encerrou com a confirmação final de shutdown. O runner foi corrigido para distinguir essas duas confirmações. Isso está registrado nos indicadores de evidência, sem ocultar a tentativa.


São 9 posições × 9 aliases × 7 dimensões = **567 pares de coordenadas por boot**, com testes independentes de referência de chunk, leitura/escrita e remoção. Incluem coordenadas dentro e fora dos limites, centro, lados e quatro cantos; os aliases são enviados à API real sem pré-dobrá-los na ferramenta. Marcadores ficam no primeiro boot e são conferidos depois de salvamento, encerramento e reabertura completa. Um marcador descartável em altitude alta não é uma medição de continuidade de montanhas/estruturas na costura.

Testes não executados/aprovados por esta suíte: evolução de líquidos/ticks/redstone na costura; contagem e persistência de UUIDs de entidades durante voltas; estrutura natural cruzando a costura; descarregamento seletivo de chunks em uma sessão; cliente, renderização, mapas, multiplayer, longas sessões e desempenho comparativo. A reabertura testa recarregamento completo de chunks e persistência dos blocos; não substitui os demais casos. Ausência de corrupção nessas operações não é garantia universal de saves. Nenhum cliente Minecraft foi aberto.

## Regressão e inventário

ZIP atual: 134 JARs, SHA-256 `f9e5866f8b2b553369edaca9c9232e346c0c76dcae77c069c66f692de19f9bbe`. O ZIP **não contém o ID strato**, contrariando a frase anterior do README. As duas versões Iris permanecem duplicadas; nada foi retirado do ZIP. O perfil servidor tem 116 JARs: base com 21 exclusões explícitas de cliente, fork, integração principal e mod separado de testes. Essa composição não aprova as duas versões Iris ou o cliente completo. A auditoria nova da entrega registra todos os 134 arquivos/hashes e os intervalos declarados; inventários anteriores não foram sobrescritos.

**Regressão aprovada nos critérios executados** com o JAR final: 170 verificações de gameplay (eventos de dano/spawn e persistência de filas TFTH), 52 casos de moldura/ignição, viagens e retorno de entidades por portais BBL, Catalyst Undergarden original e escala Nether (4000,5 → 1990,5, com tolerância de busca de moldura). Sete paletas/geradores e regras Router conferidos. [Evidência desta execução](regressao-worldgen-20261024.json), com hashes novos. Não houve cliente, multiplayer ou exame visual de costuras.

Avisos/erros de recursos terceiros continuam no log: loot tables/receitas opcionais, funções de spawnanimations com comandos de dependências ausentes, advancement Dungeons Arise, referência `minecraft:empt`, faixas de altura vazias, atributos Forge antigos e `incontrol` ao gravar áreas. Não foram ocultados removendo mods ou estruturas. A aprovação da suíte não significa log completamente sem erros de recursos de terceiros.

Preservados por diff/validação dos recursos: Terralith e 1.713 parâmetros Pokémon, 148 clones, BYG, Wover/BetterNether/BetterEnd, gerador Undergarden, escala Nether 2 e Undergarden 4, penalidade de armas, portais BBL/Catalyst e originais, seletores WDA, TFTH e suas restrições. `audit_worldgen_contract.py` compara os dados originais Undergarden/Pit retirando apenas campos de forma; também compara os overrides de gameplay com os recursos atuais dos JARs, sem renovar o manifesto histórico por conveniência.

Validação de repositório genérica encontrou problemas anteriores ao escopo: referências a `modpack/mods-list.txt` ausente e manifesto antigo incompatível com o ZIP atual. [Resultado desta tentativa](validacao-repositorio-toroidal-20261024.json). O relatório histórico foi restaurado após a ferramenta gravá-lo em caminho fixo; não foi reutilizado como aprovação nova. O contrato específico de worldgen e a auditoria da entrega conferem os dados/hashes atuais separadamente.

Site: tentativa inicial sem Astro falhou; instaladas somente dependências já declaradas, sem lockfile, e `npm run build` passou com **22 páginas**. Sem alteração de fonte/visual do site ou do base `/Minecraft-2`.

## Arquivos alterados e justificativa

| Grupo de arquivos | Mudança |
|---|---|
| `modpack/config/ars_{nouveau,elemental}-common.toml` | Desativar só regiões globais TerraBlender |
| `modpack/config/worldgenrouter/world_sets.toml`, `modpack/development/generate_worldgen.py` | Isolamento natural Nouveau/Elemental e reprodução da opção climática |
| `modpack/kubejs/data/ars_elemental/neoforge/biome_modifier/common_flashing_archwood.json` | Restaurar habitat original equivalente, sem features comuns em todos os biomas |
| `modpack/kubejs/data/m2arcana/dimension/world.json` | STRONG, mantendo largura, parâmetros e router Strato |
| `modpack/integration/src/main/java/br/com/minecraft2/WorldgenIntegration.java`, `WorldShapeDiagnostics.java`, `mixin/ArcanusClimateFieldsMixin.java`; resources `META-INF/neoforge.mods.toml`, `m2_worldgen.mixins.json` | Filtro de spawn natural, diagnóstico de forma, reconhecimento de aliases e dependência EW exata |
| `modpack/integration/src/test/java/br/com/minecraft2/test/{WorldgenSmokeTest,TopologyAudit,ArcanusAtlas}.java` | Suítes separadas de identidade/persistência/atlas, sem instalá-las no jogador |
| `modpack/development/{run_topology_audit,record_topology_audit,inspect_saved_worldgen,audit_worldgen_contract,build_delivery}.py` | Reprodução, evidência por hash e entrega com overrides |
| `README.md`, `ALTERACOES.md`, `TESTES.md`, relatórios JSON/CSV/Markdown novos em `extras/` | Estados verdadeiros, ocorrências, limites e instalação |

As alterações prévias nos scripts de compilação e na documentação de build foram preservadas; não foram atribuídas a esta correção como trabalho novo. Nenhum script de portal/KubeJS, regra de dano ou mixin TFTH foi alterado.

## Entrega e hashes

Entrega local: `modpack/integration/build/entrega-toroidal-final-20261024/`. Contém dois JARs, licença, `config/`, `defaultconfigs/`, `kubejs/`, `INSTALACAO.md`, inventário do ZIP, auditoria de intervalos e `build-report.json` com hashes de cada override. A pasta é ignorada pelo Git.

| Arquivo | SHA-256 |
|---|---|
| `m2-worldgen-1.1.0.jar` | `538cf46f66f5fb6a3cc85d1f774bed1534ac507df8338990c4023e88b8967109` |
| `strato-0.4.1-m2.1.jar` | `865451f443b83ea80e9cc5e010ac75468368610c5d75cbe1fe25a539cb4d8fa7` |

A entrega reproduziu bytecode Java 21 e passou inspeção de metadata, integridade ZIP, classes duplicadas contra os mods de topo e preservação dos inputs durante build. O fork é idêntico ao histórico; m2_worldgen mudou. `installation_blocked=true` refere-se à duplicata Iris na composição completa simulada, não a uma recusa dos JARs personalizados no servidor. Oito divergências de intervalos declarados de terceiros continuam registradas. A amostra de altura-base teve mínimo −64 e máximo 136; isso não certifica terreno gerado completo ou montanhas visuais.


JAR de testes nunca é copiado à entrega do jogador. Licença MIT do Strato acompanha o JAR e a entrega. Mods de terceiros do ZIP não são redistribuídos no pacote de atualização. A versão textual da integração permanece 1.1.0; para distingui-la da versão histórica é obrigatório usar o SHA-256 novo, não apenas o nome.

Backups próprios, sem binários de terceiros: `backup-toroidal-fontes-20261024-005936.zip`, SHA-256 `0ee95cdbed6f724c062b65e98873fc94ae15c2dabdaeb0bd1982206e9d7d9a92`, 55 arquivos verificados por leitura/igualdade; complemento `backup-toroidal-complemento-20261024.zip`, SHA-256 `eb821bd15f3b73f421554b7f130a3775a34d0caa633acafafa8225a34ccced38`, quatro fontes originais do HEAD, integridade ZIP verificada. As alterações locais de compilação existentes antes desta tarefa foram preservadas. Backups ficam planos em extras e ignorados pelo Git; não incluem saves, segredos ou JARs de terceiros.

## Instalação no CurseForge — somente cópia de teste

1. Faça backup e duplique o perfil. Não instale esta atualização na instância ativa nem abra um save pessoal com ela.
2. Confira o inventário de mods da cópia. Resolva a duplicata Iris antes de abrir o cliente, mantendo apenas uma versão compatível com seu Sodium/NeoForge. Isso não foi testado pelo servidor dedicado.
3. Na cópia, guarde fora de `mods` qualquer integração m2_worldgen anterior. Caso exista outro mod de ID `strato`, guarde-o também fora de `mods`; nunca instale duas versões de mesmo ID. O ZIP atual não o contém, mas o perfil cliente pode diferir.
4. Copie somente os dois JARs personalizados da entrega para `mods`; mantenha Exotic Worlds 1.1.4 e dependências do perfil. Não copie `m2-worldgen-tests-*.jar`.
5. Copie `config`, `defaultconfigs` e `kubejs` da mesma entrega para essa cópia do perfil. Confira os SHA-256 no manifesto. Não misture os JSONs novos com um JAR antigo, pois há codec e complemento climático Java.
6. Reinicie completamente e crie **mundo novo**, seed de teste 420211, preset **Minecraft 2 / `m2worlds:toroidal`**. Não aceite somente aparência repetida. Em servidor novo use `level-type=m2worlds\:toroidal`.
7. Confira as sete linhas `[M2 SHAPE]` no log e as larguras. Para investigar um save existente, use `inspect_saved_worldgen.py` em modo somente leitura; isso não o converte. Execute J25–J30 e os testes pendentes J01–J24 com evidências da versão/hashes atuais.

Reprodução: `python modpack/development/build_delivery.py`; compilar suíte separada com `build_integration.py --tests`; preparar servidor com o instalador oficial NeoForge no cache e usar `run_topology_audit.py` (write, reopen, `--suite regression`). O runner registra seed, inputs, exclusões e hashes, recusa resultados residuais e confere o marcador final no mesmo log. Sable pode reter a JVM após salvar; o runner finaliza apenas seu próprio processo após confirmação vanilla de salvamento completo.

Empacotamento reproduzível: `package_topology_delivery.py --delivery CAMINHO_ENTREGA --runtime extras/topologia-strong-reopen-20261024.json --regression extras/regressao-worldgen-20261024.json --atlas extras/atlas-arcanus-20261024.json --evidence CAMINHO_RELATORIO --output CAMINHO_ZIP_NOVO`. O empacotador exige que os dois JARs e todos os overrides sejam idênticos aos das três execuções. A entrega inclui `runtime-validation.json` e `MANIFESTO_COMPLETO.json`.

A evidência de preservação e correspondência da entrega está em [preservacao-entrega-toroidal-20261024.json](preservacao-entrega-toroidal-20261024.json); o contrato final de dados/fontes está em [auditoria-worldgen-entrega-20261024.json](auditoria-worldgen-entrega-20261024.json). A validação não redefine como aprovados os inventários ou os JARs históricos.
