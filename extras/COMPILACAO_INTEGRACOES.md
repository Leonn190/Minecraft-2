# Compilação das integrações — 23/10/2026

M2-023. Os dois JARs foram compilados no Windows com o JDK real do CurseForge **21.0.7**, `--release 21` e 85 bibliotecas selecionadas dos manifests de **Minecraft 1.21.1 / NeoForge 21.1.263**. Nenhum cliente ou servidor Minecraft foi iniciado. Os resultados históricos de gameplay continuam vinculados às suas execuções antigas.

## Busca antes da compilação

Não foram encontrados `m2-worldgen-1.1.0.jar` ou `strato-0.4.1-m2.1.jar` nas árvores pesquisadas: repositórios locais, CurseForge (incluindo M2D e Export), Downloads, caches Gradle/JDK e `.minecraft`. Foram examinadas também as entradas de ZIPs em Downloads, Export e nos backups planos em `extras/`. Os fontes e bibliotecas de servidor históricos não estavam no cache deste checkout. Isso registra o alcance da busca, sem afirmar ausência em discos ou backups externos.

O Java do PATH aponta para o redirecionador Oracle; a consulta por esse caminho não retornou no prazo. O build usa diretamente `bin/javac.exe` do JDK detectado, validado executando `javac -version`. Não instalou outro Java nem modificou o PATH.

## Resultado reproduzido

| Arquivo | SHA-256 | Comparação histórica |
|---|---|---|
| `m2-worldgen-1.1.0.jar` | `194b1bff7f7699eaa05ff027b7b754c59e3d46101e6aa7b7186cc838f69a2fd6` | Idêntico ao manifesto histórico |
| `strato-0.4.1-m2.1.jar` | `865451f443b83ea80e9cc5e010ac75468368610c5d75cbe1fe25a539cb4d8fa7` | Idêntico ao manifesto histórico |

Strato foi obtido do repositório Kamilhik/Stratospherical-Expansion-NeoForge, commit **e5da51aabda9801885a15a446001e159411a63ea**. O checkout limpo foi preservado; `prepare_strato.py` aplicou o patch existente numa cópia nova. Não houve execução de `--apply` nem regeneração dos recursos do repositório. A licença MIT embarcada foi comparada byte a byte à licença preservada no projeto. O fork não contém overrides `data/minecraft/`, classes do mixin global ou mixins globais habilitados.

Ambos declaram os IDs/versões esperados (`m2_worldgen` 1.1.0 e `strato` 0.4.1-m2.1), loader `javafml`, bytecode Java 21 e ZIP íntegro sem entradas duplicadas. Nenhuma classe das integrações colide com classes dos outros JARs principais na composição que substitui Strato upstream. Recompilações repetiram os mesmos hashes. O JAR separado de testes também compilou, mas ficou fora da entrega e não foi executado.

## ZIP atual e impedimentos de instalação

O ZIP atual tem **134 JARs**, SHA-256 **f9e5866f8b2b553369edaca9c9232e346c0c76dcae77c069c66f692de19f9bbe**. É diferente do inventário anterior de 122 JARs; `modpack/mods-manifest.json`, `mods-list.txt` e relatórios antigos foram preservados como evidência histórica. A auditoria nova está em [compilacao-integracoes-20261023.json](compilacao-integracoes-20261023.json), com inventário, hashes e conflitos próprios desta leitura.

- Strato upstream 0.4.6 e fork 0.4.1-m2.1 compartilham `strato`: substituir somente na cópia de teste, nunca instalar juntos. Nenhum JAR do ZIP foi alterado ou retirado.
- Há dois Iris: `iris-neoforge-1.8.12+mc1.21.1.jar` e `iris-neoforge-1.8.14-beta.1+mc1.21.1.jar`. A cópia de teste precisa manter apenas uma versão compatível, a validar; esta tarefa não escolheu ou removeu versões.
- O comparador Maven identificou oito intervalos Minecraft divergentes: Dungeons Arise, Undergarden, Ars Additions, os dois Iris, Iron Chests, JEI e More Culling. São declarações divergentes; não se inferiu aprovação do loader ou gameplay.
- Na composição simulada que troca upstream pelo fork, não há IDs obrigatórios declarados ausentes. Isso não elimina conflitos de versão/comportamento não declarados.
- A instância M2D contém 135 JARs e a mesma duplicata Iris. Seu inventário foi lido e está na entrega; ela não foi usada como destino de instalação e não equivale ao ZIP.

## Reprodução e entrega

Na raiz do repositório:

```powershell
python modpack/development/build_delivery.py
```

O comando aceita `--jdk`, `--install`, `--source` e `--instance-mods`. Usa o ZIP local como fonte das APIs. Bibliotecas ausentes com URL/hash nos manifests podem ser obtidas somente para `.cache/build-libraries`; nesta execução as 85 bibliotecas já estavam locais. Classes transformadas pelo NeoForge precedem as vanilla; nenhuma outra versão instalada entra por busca global. Artefatos Mojmap gerados ausentes produzem erro específico, sem tentar usar JARs obfuscados como substitutos.

A entrega desta execução está em `modpack/integration/build/entrega-integracoes-2026-10-23/`: dois JARs, `INSTALACAO.md`, licença Strato, `SHA256SUMS.txt`, `build-report.json`, inventários do ZIP/M2D e auditoria da composição simulada. Binários e pasta de build continuam ignorados; não houve commit, publicação ou instalação na instância ativa. A entrega contém apenas os dois binários solicitados, sem mods auxiliares de terceiros ou dados pessoais.

Foram conferidos **1.269 arquivos de código/dados** antes/depois (config, defaultconfigs, KubeJS e fontes Java), além do hash do ZIP. Fontes Java, scripts KubeJS, configurações e worldgen permaneceram idênticos. As classes de cada build usam diretório novo para impedir resíduos de compilações anteriores. A entrega exige pasta nova e recusa sobrescrever uma entrega existente.

Testes de recusa executados: JDK inexistente, API ausente no ZIP e pasta de entrega existente. A tentativa `cd site; npm run build` falhou com **Astro não reconhecido**, pois as dependências do site não estão instaladas. Nenhum arquivo do site foi alterado. Cliente, multiplayer e J01–J24 continuam pendentes/bloqueados; compilação não é teste de gameplay.

O validador geral do repositório também foi executado: verificou sintaxe de dez scripts Python, quatro JavaScript, 976 JSONs e 220 TOMLs, mas **não passou** por referências preexistentes a `modpack/mods-list.txt` (ausente neste checkout) e pelo ZIP atual diferir do manifesto histórico. Esses impedimentos não foram ocultados nem corrigidos fora do escopo. O relatório histórico `extras/validacao-repositorio.json` foi restaurado byte a byte após a execução; a falha desta execução está registrada na evidência nova. `git diff --check` passou.
