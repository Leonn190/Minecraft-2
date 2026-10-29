# Testes — Minecraft 2

## M2-028/029 — 26/10/2026

**Regra vigente:** Spore somente Pokémon; TFTH somente Arcanus. J19–J24 abaixo descrevem a política histórica M2-022 de duas dimensões; para a versão atual use J32–J34. Nenhum cliente ou servidor foi iniciado nesta alteração, conforme solicitado. Compilação não comprova gameplay.

| ID | Procedimento | Estado verdadeiro |
|---|---|---|
| AUTO-PRAGAS-CATACLYSM-20261026 | Compilar `build_integration.py`; conferir 31 overrides com `plagues_cataclysm_data.py --check`; executar `audit_worldgen_contract.py` com relatório novo; JSON/TOML, assinaturas alvo, sintaxe JS/Python, lista de 15 IDs, 11 frequências zero; conferir instalação e backup por SHA-256 | Compilação e verificações estáticas executadas; sem Minecraft. Evidências novas: `extras/auditoria-pragas-cataclysm-20261026.json`, `extras/contrato-pragas-cataclysm-20261026.json`, `extras/instalacao-pragas-cataclysm-20261026.json`. Site compilado: 22 páginas; fontes/visual inalterados. |
| J32 | Reiniciar completamente M2D, mundo novo; explorar Pokémon, Arcanus e todos os demais mundos. Observar estruturas, vegetação, spawns naturais, conversões e incubadoras dos dois mods | Pendente: Spore somente Pokémon; TFTH somente Arcanus; outros mods preservados. |
| J33 | Nas mesmas dimensões: ovos, spawners, `/summon`, portais com entidades, colocar blocos/baldes, aplicar efeitos; manter chunks com praga carregados por vários intervalos | Pendente: entidades/blocos/efeitos novos recusados fora do destino; propagação ativa somente no destino. Itens de inventário e receitas permanecem. |
| J34 | Em cópia de save antigo com mobs/blocos das duas pragas fora do destino: reiniciar duas vezes, observar filas, block entities e efeitos; repetir dentro do destino | Pendente: conteúdo antigo preservado/inativo fora; filas na dimensão correta; atividade original dentro. |
| J35 | Mundo novo, aguardar fila do script; conferir 15 mensagens de sucesso e ausência de pendências `[M2 CATACLYSM]`; visitar todos os IDs do script e respectivos chefes/loot | Pendente: exatamente uma por ID, 12 Overworld, 2 Nether, 1 End. Terreno inválido ou área usada deve ser registrado como pendência, nunca aprovação. |
| J36 | Salvar/reabrir duas vezes, usar `/reload`, explorar chunks novos e aliases toroidais; comparar starts/posições; usar `/locate` e olhos do mod | Pendente: nenhuma nova cópia natural/automática; localizadores alcançam os starts registrados. |
| J37 | Cópia de save preexistente com construções e estruturas Cataclysm: abrir e explorar; comparar áreas anteriores e chunks novos | Pendente: executor automático desativado; construções/dungeons antigas preservadas; geração natural repetida desativada. Não promete deduplicar estruturas antigas. |
| J38 | Perfil descartável: tornar região candidata já usada; simular ID ausente e falha de colocação; reiniciar após reserva persistida | Pendente: preflight recusa área usada; falhas não contam como sucesso nem criam nova cópia após reserva; entradas pendentes aparecem no log. |

Relatórios/limites: `extras/PRAGAS_CATACLYSM_20261026.md`. Não reutilizar aprovação dos servidores históricos para o JAR novo.

## Checklists anteriores

Alvo: **Minecraft Java 1.21.1 / NeoForge 21.1.263 / Java 21**. Não foi aberto cliente Minecraft nesta tarefa. Os testes J abaixo são para quando o PC principal estiver disponível; **todos estão pendentes**, exceto o bloqueio de instalação identificado. Compilar, carregar servidor e publicar eventos com entidades de teste não aprova testes manuais.

Antes de testar: cópia descartável do perfil, backup do save, hashes registrados e reinício completo. Não instale o JAR de testes em um perfil de uso normal: ele encerra o servidor. Não instale simultaneamente Strato upstream e fork isolado (mesmo ID). Guarde evidência com versão/hashes, seed, dimensão, coordenadas, passos, capturas/logs e resultado. Estados permitidos: **pendente**, **aprovado**, **reprovado**, **bloqueado**. Somente mude para aprovado após executar e conferir o resultado esperado.

| ID | Funcionalidade | Preparação | Passos objetivos | Resultado esperado | Estado |
|---|---|---|---|---|---|
| J01 | Instalação e preservação M2-002/018/023 | Perfil de teste e inventário atual; desbloquear conflitos antes de instalar | Instalar configs/defaultconfigs/KubeJS e integração 1.1.0; reiniciar; abrir JEI, criar mundo e acessar inventário | Sem erro de codec/mixin; itens, receitas e configs anteriores disponíveis | **bloqueado**: ZIP atual de 134 JARs contém duas versões Iris; auditoria de 24/10 não encontrou ID strato no ZIP. Instalar fork e upstream simultaneamente continua proibido |
| J02 | Paletas/geradores M2-010/011 | Perfil coerente; novo mundo preset `m2worlds:toroidal` | Visitar Overworld, Pokémon e Arcana; inspecionar biomas e terreno em várias regiões | BYG no Overworld, Terralith no Pokémon, terreno Strato/archwood na Arcana, sem troca global de gerador | pendente |
| J03 | Cobblemon M2-010/013/014 | Pokémon e quatro dimensões proibidas carregadas; modo sobrevivência | Observar ciclos de spawn em locais válidos; explorar vegetação e estruturas; comparar logs | Pokémon aparece naturalmente em Pokémon; recursos Cobblemon isolados; nada espontâneo fora da dimensão | pendente |
| J04 | Ars M2-011/013/015 | Arcana, condições ambientais dos mobs atendidas | Explorar archwood e estruturas; observar addons na Arcana e fora; invocar manualmente fora | Geração/spawn natural conforme política; invocação manual e itens utilizáveis; habitats Fauna ausentes não inventados | pendente |
| J05 | Limites toroidais M2-012 | Mundo novo, larguras de ALTERACOES; Elytra/voo de teste | Cruzar quatro bordas e cantos nas sete dimensões; repetir com mob seguindo; explorar estruturas de costura | Continuidade sem parede invisível, perda de entidade ou corrupção; geradores preservados | pendente |
| J06 | Escalas Nether/Undergarden M2-012 | Portais longe e perto das bordas | Viajar ida/volta em ambos, registrar coordenadas e repetir após reinício | Escala Nether 2 e Undergarden 4, retorno e dobramento corretos | pendente |
| J07 | Alex's Mobs M2-013 | Pokémon, Arcana e Overworld com locais válidos | Observar terrestres e baleias; comparar com Overworld; usar ovo/comando nos três | Sem geração natural/ambiental nos dois mundos isolados; original fora; manual continua permitido | pendente |
| J08 | Desbloqueios M2-014/015/016 | Jogador novo sem tags `m2p_unlocked`/`m2a_unlocked` | Entrar duas vezes em cada dimensão; usar `/trigger m2a_enter set 1` e `/trigger m2a_leave set 1` nos lados válidos | Iniciais/notebook uma vez; tags corretas; triggers e resistência temporária preservados | pendente |
| J09 | Molduras/ignição M2-017 | Catalyst, ferro, redstone, crying obsidian; guia em extras | Construir 4×5 e 4×6 nos eixos X/Z; clicar em cima/baixo/laterais; testar canto errado, metades invertidas e Flint and Steel | Pokémon vermelho e Arcana roxo só com moldura válida/Catalyst; item não consumido; portal Undergarden original funciona | pendente |
| J10 | Links e multiplayer M2-017 | Dois jogadores, dois portais de cada tipo | Viajar/retornar, repetir após cooldown e após reinício; destruir/obstruir destino; testar bordas | Vínculos independentes, saída segura, plataforma correta, retorno persistente; sem teleporte trocado | pendente |
| J11 | Receitas/equipamentos M2-018 | Inventário de itens anterior e JEI | Comparar receitas, loot e usos de Ars/Create/Cobblemon; equipar armaduras/usar mesas próprias TFTH fora das dimensões | Recursos anteriores preservados; nenhuma nova alteração de receita/loot atribuída a esta tarefa | pendente |
| J12 | Armas de contato Undergarden M2-020 | Alvo sem armadura/efeitos, arma sem encantamentos; instrumento/log para dano | Comparar espada diamante, machado netherita e arma de outro mod dentro/fora; mesma força de ataque | Entrada de dano no evento ×0.40 só no Undergarden; atributos do item iguais antes/depois | pendente |
| J13 | Arma nativa M2-020 | Espada/clava Undergarden e alvo controlado | Repetir J12 usando arma namespace `undergarden`, incluindo encantamento conhecido | Dano recebido pelo listener preservado; sem penalidade duplicada | pendente |
| J14 | Projéteis M2-020 | Arco, besta, tridente; alvo e distância fixos | Disparar e trocar/soltar arma antes do impacto; repetir fora e em Undergarden | Item original determina a regra, sem consultar mão atual; disparo sem item identificável documentado e inalterado | pendente |
| J15 | Magia/mobs M2-020 | Mob armado e arma mágica que exponha item no projétil | Comparar golpes de mob, projétil mágico identificável e habilidade sem arma; registrar classe/API da origem | Jogadores e mobs seguem mesma regra; magia identificável penalizada; habilidade sem prova de arma não alterada | pendente |
| J16 | Exclusões e servidor M2-020 | Servidor com dois clientes, alvo controlado | Testar punhos, maçã na mão, queda, fogo, lava, espinhos; repetir arma 20 vezes | Exclusões intactas; somente um ×0.40 por evento, sem alteração cumulativa de atributos | pendente |
| J17 | WDA permitido M2-021 | Mundo novo, seed registrada, 14 IDs do manifesto | Usar `/locate structure dungeons_arise:ID` para cada ID e explorar chunks novos em habitats originais | Os 13 grupos continuam elegíveis. `mining_system` não é acrescentado aos sets originais: ausência natural isolada não prova bloqueio do grupo `mining_complex` | pendente |
| J18 | WDA bloqueado/preservação M2-021 | Cópia de save com estrutura antiga e região nova; manifesto | Visitar estrutura antiga; localizar/explorar candidatos novos dos 26 bloqueados e estruturas de outros mods | Antigas permanecem; bloqueadas sem habitat natural; outros mods preservados. `/place` manual não é prova de geração natural | pendente |
| J19 | TFTH todas as fontes M2-022 | Pokémon/Arcana e Overworld/Nether/End/Undergarden/The Pit; condições de estágio válidas | Observar spawns por blocos, estrutura, incubadora, evento; testar spawner, ovo e comando em cada dimensão | Natural funciona só nas duas permitidas; filtro de inserção também impede invocação manual fora delas, conforme limitação documentada | pendente |
| J20 | Carne/vegetação/pus M2-022 | Áreas idênticas com carne marcada para propagação, vegetação e pus nas dimensões da J19 | Registrar cubo de blocos; manter chunks carregados por pelo menos dois intervalos configurados; comparar | Só Pokémon/Arcana propagam/convertem/geram vegetação e fluxo; fora permanece inativo sem apagar blocos | pendente |
| J21 | Incubadoras M2-022 | Jogador próximo e estágio/chances originais; incubadoras existentes em cópia de save | Aguardar intervalos configurados em todas as dimensões; provocar condições de geração | Incubadoras e seus mobs funcionam nos dois mundos; sem novas incubadoras/blocos associados fora | pendente |
| J22 | Conversão/infecção/eventos M2-022 | TFTH existente em save de teste, vítima, efeito One of Us; estágios controlados | Provocar morte, ataque contaminante e contato com pus/carne dentro/fora; observar mensagens/sons em dois clientes | Infecção espontânea e mudanças ambientais só nos permitidos; estágio compartilhado entre eles; jogadores fora não recebem anúncio de fase | pendente |
| J23 | Persistência de filas M2-022 | Áreas de propagação separadas em Pokémon/Arcana; fila antiga sem campo de dimensão | Salvar, encerrar completamente e reabrir duas vezes; comparar coordenadas/blocos nas três áreas | Filas novas retomam na dimensão correta; fila antiga do Overworld fica inativa; nenhum bloco migra de mundo | pendente |
| J24 | TFTH permitido e saves antigos M2-022 | Save cópia com mobs/blocos fora e dentro das dimensões permitidas | Reabrir; observar mobs fora; testar itens, receitas e efeitos visuais; explorar mushroom_fields clones | Mobs antigos fora preservados e inertes; dentro comportamento original e seis estruturas elegíveis; itens/receitas/visuais mantidos | pendente |

## Presets normais — M2-027 (25/10/2026)

| ID | Procedimento | Estado verdadeiro |
|---|---|---|
| AUTO-PRESET-20261025 | `audit_worldgen_contract.py --output extras/auditoria-presets-normais-20261025.json`; comparar `minecraft:normal` e `wover:normal` com `m2worlds:toroidal`, larguras, recursos e ZIP atual; conferir instalação dos dois JSONs e integridade do backup | Contrato estático aprovado; arquivos instalados em M2D idênticos ao repositório. Evidência: `extras/instalacao-presets-normais-20261025.json`. Nenhum save ou JAR modificado. |
| AUTO-PRESET-RUNTIME-20261025 | Servidores NeoForge 21.1.263 descartáveis, Exotic Worlds 1.1.4, seed 420211: criar pelo `minecraft:normal`, salvar/reabrir; criar outro pelo `wover:normal`. Comparar identidade de chunks e leitura/escrita/remoção por nove aliases em nove posições nas sete dimensões | **Aprovado nos três boots**: 567 comparações por boot; 63 marcadores persistidos na reabertura do Normal. Overworld 9.984, Nether 4.992 e End 6.016; BetterX e escala Nether 2 preservados. Salvamento completo confirmado nos três encerramentos. Evidência com hashes próprios: `extras/runtime-presets-normais-20261025.json`. Não equivale a teste do cliente M2D. |
| J31 | Reiniciar completamente M2D; criar mundo novo com a escolha Normal padrão; visitar Overworld/Nether/End; cruzar lados e cantos, colocar/remover blocos, salvar e reabrir; repetir escolhendo Normal Wover explicitamente, se oferecido | Pendente no cliente. Larguras esperadas: 9.984/4.992/6.016 blocos. Nether/End preservam BetterX e escala Nether 2. Repetição visual/F3 isolada não aprova identidade de armazenamento. |

O relato do usuário corresponde à versão anterior; não aprova J31 após a instalação. O mundo descartável existente foi preservado conforme solicitado. `generate_worldgen.py` reproduz os dois presets sem editar outras dimensões; sintaxe dos três scripts Python alterados conferida por `py_compile`. `npm run build` aprovado: 22 páginas, sem alteração visual do site. `git diff --check` executado.

Reprodução de servidor em pastas novas do cache: `run_topology_audit.py --server CAMINHO` testa `minecraft:normal`; `--preset wover:normal` testa o outro caminho. `--reopen` exige o mesmo preset usado na criação. A primeira tentativa no sandbox falhou antes da suíte por `Permission denied: connect` no socket local; não recebe aprovação nem substitui as evidências de uma execução concluída. O JAR de testes permanece somente nos servidores descartáveis.

## Validação automatizada (separada dos testes de cliente)

| ID | Execução e critério | Evidência/estado |
|---|---|---|
| AUTO-INVENTARIO | Abrir ZIP e cada JAR/JarJar, metadados e SHA-256; comparar manifesto anterior/launcher; checar intervalos Maven | Executado: 122 JARs, +16/−5 nomes, sem hashes divergentes nos comuns, sem duplicatas principais/hash ou IDs obrigatórios ausentes; seis intervalos divergentes. `modpack/mods-manifest.json`. Não é aprovação do conjunto no jogo. |
| AUTO-BUILD-WINDOWS | M2-023: `python modpack/development/build_delivery.py`; compilar também `build_integration.py --tests`; verificar bytecode 65, metadata, licença, conflitos Maven, integridade e SHA-256 | **aprovado para compilação/inspeção estática em 23/10/2026**: JDK 21.0.7, 85 bibliotecas; ambos os JARs idênticos aos hashes históricos. 1.269 arquivos de código/dados idênticos antes/depois. Recusas verificadas para JDK inexistente, API ausente e pasta de entrega existente. ZIP atual: 134 JARs, hash novo, duplicata Iris, oito intervalos declarados divergentes; sem IDs obrigatórios ausentes na composição simulada com fork substituindo upstream. M2D: 135 JARs, duplicata Iris. `extras/compilacao-integracoes-20261023.json`. Nenhum Minecraft iniciado. Site: tentativa nesta tarefa falhou porque Astro não está instalado; AUTO-SITE abaixo é histórico. |
| AUTO-WDA | `python modpack/development/gameplay_data.py --check`: 40 IDs classificados, 14 permitidos e 26 bloqueados; conteúdo bloqueado idêntico ao original exceto biomes; sem overrides de permitidos/structure sets; seis seletores TFTH com clones existentes | Executado; detalhes em `modpack/gameplay-structure-manifest.json`. |
| AUTO-JAVA | Compilar integração principal e mod separado de testes com `--release 21`, APIs do ZIP atual e bibliotecas NeoForge 21.1.263 | Executado; JARs locais ignorados em `modpack/integration/build/`. Compilação não prova compatibilidade do conjunto novo. |
| AUTO-GAMEPLAY | Servidor dedicado descartável, eventos reais de dano/spawn e roundtrip NBT das filas em nove dimensões, sem cliente | **aprovado: 153 verificações**; relatório final em `extras/gameplay-runtime-validation.json`, vinculado aos hashes efetivamente testados. O perfil de servidor usa a base histórica e fork Strato; não certifica os 122 JARs atuais. |
| AUTO-REGRESSAO | Suíte existente de geração/portais mais gameplay, com a integração 1.1.0 no mesmo servidor descartável | **aprovado**: 153 verificações de gameplay, 52 casos de moldura/ignição e viagens de entidades, sete dimensões e Router válido. `extras/regressao-runtime-validation.json`. Não foi solicitada a regeneração extensa de chunks de borda (`m2.audit.chunks` desativado); cliente e multiplayer continuam pendentes. |
| AUTO-REPO | `python modpack/development/validate_repository.py`: JSON/TOML/dados próprios, sintaxe JS/Python, links Markdown locais, inventário/hashes, integridade dos backups/movimentos, preservação dos recursos antigos e diff | Resultado em `extras/validacao-repositorio.json`. Temporários/JSON5 com comentários têm formato próprio; não são interpretados incorretamente como JSON estrito. |
| AUTO-SITE | `cd site; npm run build` | **aprovado nesta execução**, quatro páginas; sem alteração visual/código do site e base `/Minecraft-2` preservada. |

Validações anteriores de mundo/portais (incluindo 52 casos) estão em `extras/worldgen-runtime-validation.json` e `extras/VALIDACAO_WORLDGEN.md`, com hashes antigos. Não foram transformadas em aprovação de J01–J24. Avisos conhecidos de terceiros em loot/receitas/metadata de packs permanecem documentados no relatório histórico. Nenhuma evidência é obtida de uma execução incompleta ou de um JSON residual de outro boot.

Para reproduzir o teste de servidor: compilar com `--tests`, preparar perfil descartável coerente, usar `-Dm2.audit.gameplayOnly=true` e iniciar NeoForge 21.1.263. O resultado é `m2-gameplay-audit.json`; confira a linha `[M2 GAMEPLAY] Resultado` **no mesmo log**, ausência de erros e hashes dos JARs antes de registrar aprovação. Sem a propriedade, o mod separado também executa a suíte anterior de geração/portais. A biblioteca nativa Sable pode manter o processo Java vivo após o servidor encerrar: finalize apenas o processo daquele teste antes do próximo boot, para não bloquear a DLL local.

## Auditoria toroidal de 24/10/2026 — M2-024/025/026

Os testes J01–J24 continuam com os estados anteriores. Identidade de armazenamento no servidor não aprova travessia visual, multiplayer ou gameplay no cliente. Consulte [relatório detalhado](extras/CORRECAO_TOROIDAL_20261024.md).

| ID | Procedimento e evidência | Estado verdadeiro |
|---|---|---|
| AUTO-TOPO-20261024 | Servidor NeoForge 21.1.263 descartável, seed 420211, preset `m2worlds:toroidal`; sete dimensões, nove posições (centro, lados e quatro cantos), nove aliases por posição (X/Z −L, 0, +L), mesma instância de chunk, escrita e remoção por aliases; salvar/encerrar/reabrir e conferir marcadores | **aprovado para armazenamento no servidor final**: 567 comparações por boot, 63 marcadores confirmados após reinício; evidências `extras/topologia-strong-{write,reopen}-20261024.json`. Não aprova cliente ou gameplay de costura |
| AUTO-ARS-20261024 | Inspeção da paleta Overworld e features efetivamente injetadas após biome modifiers; preservar BYG e features Ars nos clones Arcanus; conferir Router e filtros naturais | **aprovado para os critérios inspecionados**: paleta Overworld com BYG e sem Ars, zero injeções Ars em vanilla/BYG, 215 referências de features Ars nos clones Arcanus. Exploração e ciclo real de todas as criaturas continuam pendentes |
| AUTO-CLIMA-20261024 | Grade espacial 64×64 blocos em toda Arcanus: 2.209 pontos, Y=64; comparação OFF/AUTO/STRONG. Versão ampliada também amostra na altura-base retornada pelo gerador. Contexto real Exotic Worlds e sampler do servidor | **executado**: OFF 12, AUTO 16, STRONG 19 biomas nos 2.209 pontos. Deserto e badlands presentes no STRONG; archwood não observado nessa grade final, oceanos/montanhas também ausentes. Sem afirmar ocorrência dos 51 IDs; JSON/CSV em extras |
| J25 | Mundo novo final: explorar Overworld/BYG e Arcanus; observar archwoods, florestas Ars e estruturas; observar todos os addons; testar itens/magias/invocação manual fora | pendente |
| J26 | Seed 420211, repetir com outra seed; visitar coordenadas do relatório espacial; registrar biome F3 e vegetação de desertos, planícies, florestas, badlands e montanhas observados | pendente; bioma não observado não é automaticamente inacessível em todas as seeds/alturas |
| J27 | Caminhar/voar pelos quatro lados e cantos em cada dimensão; colocar/remover blocos dos dois lados; descarregar chunks e voltar; salvar/reabrir duas vezes | pendente no cliente; somente a parte de armazenamento/reabertura automatizada recebe aprovação separada |
| J28 | Nas costuras: soltar água/lava, observar fluxo e ticks, redstone, mobs seguindo, projéteis, montarias e itens; contar UUIDs antes/depois | pendente |
| J29 | Estruturas cruzando costuras, portais BBL/Nether/Undergarden próximos às bordas, dois jogadores, retorno após reinício | pendente; regressão automatizada de portais tem cobertura própria e não substitui este teste |
| J30 | Mapas/minimapas do perfil, waypoints e coordenadas efetivas após cruzar costuras; verificar ausência de duplicação/perda de entidades, corrupção e travamentos durante sessão prolongada | pendente |

Reprodução: compilar integração e `--tests`; instalar servidor oficial em uma subpasta nova de `modpack/.cache`; `python modpack/development/run_topology_audit.py --server CAMINHO --prepare-only`; aceitar o EULA oficial nesse servidor; executar sem `--prepare-only`, depois `--reopen`. `--suite regression` executa a suíte existente de gameplay/portais no mesmo mundo descartável. O runner recusa saves sem seu marcador e não instala na instância ativa. Requer socket local em 127.0.0.1:25579. Preserva os JSONs e registra hashes; Sable pode exigir finalizar somente o processo filho após confirmação vanilla de salvamento completo.

Checks adicionais nesta execução: `gameplay_data.py --check` falha na comparação do manifesto histórico com o ZIP novo, depois de conferir os recursos; `audit_worldgen_contract.py` valida os recursos atuais sem sobrescrever aquele histórico. `validate_repository.py` encontrou referências legadas a `mods-list.txt` ausente e inventário antigo; seu relatório novo foi preservado separadamente e o histórico restaurado. A tentativa inicial de `cd site; npm run build` falhou por Astro ausente; após instalar somente dependências já declaradas, sem lockfile, o build passou com 22 páginas. Nenhum arquivo-fonte do site foi alterado.

Regressão nova com hashes finais: **170 verificações de gameplay e 52 casos de moldura/ignição aprovados**, viagens BBL, Catalyst Undergarden e escala Nether preservados; sete dimensões/Router válidos. Evidência: [regressao-worldgen-20261024.json](extras/regressao-worldgen-20261024.json). Os avisos/erros de recursos terceiros estão discriminados no relatório; a suíte não certifica o cliente completo. Atlas denso de biomas usa `run_topology_audit.py --suite biomes` em outro servidor novo e o JAR de testes recompilado; nunca no perfil do jogador.

Atlas denso final: [atlas-arcanus-20261024.json](extras/atlas-arcanus-20261024.json), 565.504 pontos X/Z, Y=64, seed 420211; 19 biomas e ausência de archwood/montanhas/oceanos nesse plano. J26 permanece pendente; a ocorrência natural de todos os habitats Ars e dos 51 IDs não foi aprovada. A combinação experimental AUTO + mínimo climático teve contagens idênticas e foi descartada.


## PR experimental — checklist das três alterações (29/10/2026)

**Não executados neste ambiente:** compilação completa (sem mods.zip, bibliotecas Minecraft/NeoForge e runtime), servidor real, cliente, multiplayer, geração de chunks, salvamento/reabertura e mapas do Alex's Caves. Os testes anteriores registrados neste arquivo não homologam este novo código.

### Verificação estática isolada de reservas

Compila apenas os arquivos sem dependências Minecraft, em Java 21:

```bash
mkdir -p /tmp/m2-layout
javac --release 21 -d /tmp/m2-layout \
  modpack/integration/src/main/java/br/com/minecraft2/gameplay/CaveRegionLayout.java \
  modpack/integration/src/test/java/br/com/minecraft2/test/CaveRegionLayoutTest.java
java -cp /tmp/m2-layout br.com.minecraft2.test.CaveRegionLayoutTest
```

O teste cobre 201 seeds sintéticas, ordem determinística, seis regiões com clima aceito, limites de wrapping, separação mínima e recusa quando falta clima. **Não testa o resultado efetivo de worldgen.** A CI deste PR executa esse teste; registrar o resultado de execução somente depois de observar os logs.

### Testes manuais obrigatórios em perfil descartável

1. Em Arcanus, o braseiro `Summon Wilden` com Wing/Horn/Spike invoca Chimera; sem os três, o ritual usual continua.
2. Overworld, Nether, End e dimensão Pokémon: oferecer cada augmento no braseiro mantém a pilha intacta, mostra aviso; boss não aparece com ovo/comando; itens e feitiços comuns Ars funcionam.
3. Corrida: com stamina, decremento de 3/tick, velocidade vanilla ×1,05, sem exaustão alimentar; esgotada, sprint não interrompe, velocidade vanilla e exaustão 2×. Testar retorno da stamina após descanso.
4. Caminhada, água, planagem, PvP, dano, mineração, Stamina Vessels, morte, reconexão e troca de dimensão sem efeitos indevidos/duplicação ou divergência em servidor dedicado.
5. Para seeds 0, 1, 42 e outras geradas aleatoriamente em mundos **novos**, confirmar as seis regiões pelo bioma/subsolo, recursos, estruturas, mobs, mapas e comandos. Checar especificamente Chasm em oceano, dimensões secundárias e bordas ±4992.
6. Salvar, parar, reabrir, repetir consultas dos mapas e verificar costuras sem duplicação e sem efeito em chunks preexistentes.
7. Não instalar dois Iris nem dois Strato com o mesmo mod ID; usar fork de Strato do projeto e remover duplicata Iris **apenas na cópia de teste**.

**Bloqueios conhecidos:** cobertura climática universal, prova de features e restrição automática exclusiva a mundos novos não estão garantidas pela suíte matemática. Não confundir CI verde com homologação gameplay.
