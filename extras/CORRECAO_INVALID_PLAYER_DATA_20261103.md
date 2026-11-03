# Minecraft 2 — câmera nula e Invalid player data, 03/11/2026

## Diagnóstico confirmado

Base: main `820de1179b5c4e7c5120d707198ddc640607125d`, incluindo `71d4cc2f` e `d044aa52`. Perfil confirmado na interface CurseForge e no `minecraftinstance.json`: M2D, Minecraft 1.21.1 / NeoForge 21.1.263. Os dois erros originais de 17:31 e 17:38 estão no log real, preservado localmente antes dos testes; nenhum save pessoal foi aberto.

O engine JarJar Exotic Worlds **1.2.5** desreferencia o resultado de `ServerPlayer.getCamera()` em `ClientPosition.followCamera`. O bytecode do JAR instalado confirma ausência de guarda. `TranslationContext.of` chama esse método ao construir contexto; `PacketTranslator.dispatch/toClient` e `ConnectionMixin.translateOutgoing` levam a exceção até o envio de pacotes no login. Não há evidência para atribuir esse erro a corrupção de save, ModernFix, KubeJS, Cobblemon ou Sophisticated Core.

O export de classes efetivamente transformadas (`-Dmixin.debug.export=true`, somente na cópia de teste) revelou a origem do retorno nulo: o getter vanilla retorna o próprio jogador quando o campo é nulo, mas foi substituído por um accessor bruto de `com.railwayteam.railways.mixin.conductor_possession.ServerPlayerAccessor`, do **Railways 0.2.1**. A anotação `MixinMerged` identifica esse mixin; o getter carregado lê o campo e retorna diretamente. Não é inferência baseada apenas no nome de um mod.

Uma primeira correção somente em `ClientPosition` foi **insuficiente**: o jogador avançou no login, mas o som da entrega de um item do Alex's Mobs chegou a `ListenerCopies.nearestTo`, que também acessava a posição da câmera nula. Essa tentativa falhou e seu log foi preservado localmente. O patch final cobre os dois consumidores de `ServerPlayer.getCamera` encontrados no engine instalado. Os outros consumidores identificados são do cliente e consultam a câmera do cliente com semântica própria.

## Implementação e preservação

Dois redirects pequenos no JAR próprio `m2-worldgen-1.1.0.jar`, ativados em `m2_worldgen.mixins.json`: `ExoticClientPositionMixin` e `ExoticListenerCopiesMixin`. Cada um substitui somente o retorno nulo por `player`. A câmera padrão continua produzindo posição nula na sobrecarga original de `followCamera`; outra entidade continua com sua posição e dimensão originais. A implementação original mantém `cameraCopy`, saída de espectador, rebase, seleção de ouvintes, nearest-copy, deduplicação e processamento de pacotes.

Não se captura NPE, não se cancela pacote, não se troca gerador e não se altera globalmente `ServerPlayer.getCamera/setCamera`. O patch não pretende reparar todas as interações do accessor do Railways. Testes visuais completos de possessão de condutor continuam fora da aprovação.

Comparação com o JAR real anterior: **duas classes adicionadas, somente a lista de mixins alterada, zero entradas removidas; todas as classes e demais recursos anteriores idênticos**. Permanecem M2-028/029, mochilas, minérios, água envenenada, portais, fork Strato e intervalo Core de M2-034. Nenhuma configuração de dimensões ou arquivo KubeJS de produção foi modificado.

Foi consultado o [upstream Minecraft 1.21.1 no commit b966feb4](https://github.com/anyttng/exotic_worlds/tree/b966feb4fdb871a59db4025153198c45c3c2036b): declara 1.3.1 e ainda possui ambos os acessos inseguros. A coleção Releases do GitHub estava vazia. Portanto não foi identificada uma atualização oficial com esta correção verificável; não se fez downgrade nem se presumiu compatibilidade de outra versão. Licença declarada do bundle: PolyForm Shield 1.0.0. Não foi criado fork, copiado fonte upstream para o Git ou redistribuído JAR de terceiro.

## Artefatos e testes

| Artefato | SHA-256 |
|---|---|
| m2-worldgen 1.1.0 anterior (M2-034) | `72c6f8f6f72eacec03a5636f02dc141acb4d9d35039db1151a4800ba50748d46` |
| m2-worldgen 1.1.0 corrigido | `f416979c7d86d63109b38e4036d9b53cc7b3603d94f43fa33cf04da9be055f4e` |
| Exotic Worlds bundle 1.2.5, intacto | `cf3963da3f10bb1d283942eec6b7af7cfa5b812c55902c8f6647930a6f0e6252` |
| Engine NeoForge JarJar 1.2.5, intacto | `9672bf7d470d56267d0c9a64e1423c9666ab5eb0a8bb8ae00675fc2b75fee0b8` |

Compilação: Microsoft OpenJDK 21.0.12.1, `--release 21`, bytecode 65, APIs reais do perfil. Metadata NeoForge e requisito Exotic Worlds `[1.2.5]` preservados. O aviso deprecated preexistente em `PlagueWorldgenPlacementMixin` não impediu compilação.

Cliente descartável em `modpack/.cache/invalid-player-20261103/client`, com cópia dos 148 JARs, configs/defaultconfigs/KubeJS e preferências; **nenhuma cópia de saves pessoais**. Identidade local sintética M2Audit, sem credenciais pessoais. O JAR extra de auditoria não modifica o login e registra comando somente com marcador local próprio. Os testes do patch final ocorreram fora do sandbox: a primeira tentativa em sandbox bloqueou sockets locais, não valendo como teste de jogo aprovado.

Resultados finais: sobrevivência entrou no ambiente 3D, salvou, saiu e reentrou; mundo novo em criativo com `m2worlds:toroidal` entrou. Visitas reais às sete dimensões em espectador, **77 verificações de câmera** e **63 comparações de identidade de chunks** aprovadas. São nove aliases X/Z −L, 0, +L do chunk (0,0) de cada dimensão, não uma auditoria espacial completa. Um boot adicional, sem JAR/script de testes e com os mesmos 148 JARs de produção, reabriu a sobrevivência após reinício completo e salvou normalmente. O histórico do save avisa `m2_camera_tests -> MISSING`, mas não havia esse mod instalado. Resultado derivado e hashes de logs: [runtime-invalid-player-20261103.json](runtime-invalid-player-20261103.json). Logs brutos e saves permanecem locais. As cinco assinaturas de falha tiveram zero ocorrências tanto na sessão final com auditoria quanto no boot de produção. `TESTES.md` distingue estes testes delimitados da cobertura restante.

Na sessão extensa com auditoria e render 16/heap 8 GiB houve lentidão de geração e aviso de 90% de memória. O boot final usou heap 12 GiB, render 6/simulação 4 somente na cópia; isso não altera preferências do perfil real nem é garantia de desempenho prolongado. No teste manual de borda foi possível posicionar em X=4991,99/Y=180, mas o input w automatizado não gerou movimento sustentado; travessia visual permanece pendente. Visão de espectador acompanhando entidade viva e Normal Wover selecionado na interface também não recebem aprovação nesta execução.

Geradores gravados: [Normal/Default](geradores-normal-camera-20261103.json) e [toroidal explícito](geradores-toroidal-camera-20261103.json). Leitura somente de WorldGenSettings dos saves descartáveis, sem exportar jogador, inventário ou seed.

As sete larguras foram observadas em runtime: Overworld 9.984, Nether 4.992, End 6.016, Pokémon 4.992, Arcanus 3.008, Undergarden 4.992 e The Pit 2.016, ambas as direções. BetterX e escala Nether 2 continuam nos geradores carregados. O [contrato estático novo](contrato-invalid-player-20261103.json) inclui a leitura do ZIP atual por seu próprio SHA-256, preservando auditorias anteriores; o ZIP histórico continua separado do perfil ativo.

Reproduzir build: `python modpack/development/build_integration.py --profile CAMINHO_M2D`. Para auditoria, compilar também com `--camera-tests`, instalar **somente em cópia descartável**, criar marcador `m2-disposable-client.json`, mundo criativo `M2-035 Toroidal Creative`, comandos permitidos e jogador local M2Audit. `/m2cameraaudit` grava JSON novo no diretório do cliente. O cenário opcional `modpack/development/camera_runtime_scenario.js` pode ser copiado apenas para os server_scripts dessa cópia: `/reload` visita sete dimensões, confere larguras/63 aliases de chunk e executa a auditoria de câmera em cada uma. O script exige o comando protegido pelo marcador e nome exato de mundo. Não instalar nenhum desses instrumentos no M2D real.

## Erros secundários separados

- `pack_format`: `kubejsarsnouveau-1.3.2.jar` contém literalmente placeholder não expandido no `pack.mcmeta`. JSON inválido confirmado dentro do JAR. Aparece antes do login e também durante sessões bem-sucedidas: problema independente do NPE. Não se adulterou esse binário externo para silenciar o aviso.
- Cobblemon `.old`: avisos de `tm_moves`, `pokedex` e dados do jogador também apareceram com UUID sintético em mundo novo e login bem-sucedido. Não provam corrupção de saves pessoais nem são exclusivamente consequência da desconexão. Nenhum arquivo de jogador foi apagado ou recriado para tratar o erro.
- Iris: uniforms como `BIOME_PALE_GARDEN` e `endFlash*` não resolvidos no shader selecionado; o cliente continuou funcionando. Advertências de shader independentes, sem relação demonstrada com o NPE de câmera; shader e preferências reais preservados.

Build do site aprovado: 22 páginas, sem alterações de fontes ou visuais. Não equivale a teste de gameplay. Sessões prolongadas, multiplayer, todos os portais/projéteis/montarias, possessão Railways e travessia visual de todos os lados/cantos continuam exigindo seus próprios testes.

## Instalação no M2D e consistência

Somente `mods/m2-worldgen-1.1.0.jar` foi substituído no perfil real após o teste descartável e encerramento de Java. Backup local novo e anterior reabertos/conferidos por SHA-256; troca por arquivo temporário conferido e substituição atômica. **147 outros JARs e 268 arquivos dos saves reais idênticos antes/depois**, sem abrir mundos pessoais; nenhum JAR externo, config, preferência ou KubeJS de produção modificado. Evidência: [instalacao-camera-20261103.json](instalacao-camera-20261103.json). O perfil real não foi iniciado durante esta tarefa: a validação de jogo ocorreu em cópia com os mesmos JARs, incluindo um boot sem instrumentos de teste.

Manifestos e lista de mods atualizados com versões/hashes reais. [Inventário atual](auditoria-perfil-camera-20261103.json): 148 JARs (146 externos + duas integrações), nenhum ID principal duplicado e nenhum ID obrigatório identificado ausente. A comparação com 01/11 inclui oito substituições externas que já existiam antes desta tarefa, incluindo Core 1.5.25; nenhuma foi executada por este patch. [ZIP auditado separadamente](auditoria-perfil-camera-20261103-zip.json), intacto, SHA-256 `f9e5866f8b2b553369edaca9c9232e346c0c76dcae77c069c66f692de19f9bbe`; não determina os mods ativos. Auditorias e manifestos históricos preservados por seus próprios hashes. Backup de fontes e de build permanece local/ignorado em extras, com nomes únicos; JAR antigo local/ignorado no cache, nunca enviado ao Git.

Rollback: com o jogo encerrado, restaurar somente o JAR próprio do backup indicado na evidência e verificar SHA-256 anterior; isso também restaura o defeito de câmera. Não copiar o mod de testes, não retirar Exotic Worlds/Railways e não trocar ou converter saves. Para reinstalar a correção, compilar com as APIs reais e substituir somente esse JAR próprio após backup e validação em cópia descartável.
