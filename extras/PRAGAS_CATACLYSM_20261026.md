# Pragas dimensionais e Cataclysm único — 26/10/2026

M2-028 e M2-029 implementados no repositório e instalados no perfil local **M2D** do CurseForge. **Nenhum cliente ou servidor Minecraft iniciado**, conforme solicitado. Reinício completo necessário para carregar o JAR e os mixins novos; `/reload` sozinho não instala essa atualização.

## Política atual

| Mod | Geração, spawn e propagação |
|---|---|
| Spore 1.21.1 / 2.2.0j NeoForge | Somente `m2pokemon:world` |
| The Flesh That Hates 1.21.1 / 1.1b-port-0.2 | Somente `m2arcana:world` (Arcanus) |

Estruturas/folhagem Spore foram remapeadas para clones dos habitats originais Pokémon; TFTH ficou somente no clone de mushroom fields Arcanus. Spawns Spore usam sua tabela configurada nos clones Pokémon. Router, filtros de entidades e mixins de ambiente complementam o isolamento, incluindo propagação, block entities, efeitos, comandos/ovos e novas colocações de blocos fora do destino. Mobs/blocos antigos não são apagados e ficam inertes fora do habitat. Itens em inventários e receitas permanecem registrados; não há remoção de inventários. Dados internos das pragas podem residir no Overworld sem gerar infestação ali.

## Dungeons únicas

[Script com os 15 IDs e coordenadas](../modpack/kubejs/server_scripts/cataclysm_unique.js), [executor](../modpack/integration/src/main/java/br/com/minecraft2/gameplay/UniqueCataclysm.java). São 12 estruturas no Overworld, duas no Nether e uma no End, incluindo as estruturas menores do Cataclysm 3.33. Geração natural repetida dos 11 structure sets desativada por frequência zero; demais propriedades originais preservadas.

O executor funciona automaticamente em **mundo novo**, confirmado antes da geração de terreno por tempo zero e ausência de regiões `.mca`. Uma tentativa a cada 200 ticks, no máximo 64 candidatos por ID, em células próprias dentro dos limites toroidais. Usa as peças/alturas/verificações de terreno originais com filtro de bioma dispensado na colocação dirigida; a célula não é obrigada a apresentar o habitat original. Registra starts e referências para os localizadores do jogo/mod; funcionamento deles pendente em runtime.

Não coloca em áreas carregadas ou em regiões já salvas: confere toda a caixa da estrutura e margem de um chunk antes de gerar chunks. Estado persistente por ID em `data/m2_unique_cataclysm.dat`, no Overworld; salva reserva antes de escrever e resultado após salvar os chunks. Erro parcial suspende aquele ID para não tentar outra cópia automaticamente. Reinício e `/reload` consultam o registro persistente. Não apagar esse arquivo para regenerar.

**Verificação futura:** criar mundo novo depois do reinício e conferir as mensagens `[M2 CATACLYSM]` no log. É preciso ver 15 sucessos e visitar as 15 estruturas para aprovar a tarefa em gameplay. Uma falha de terreno, área ocupada ou ID ausente permanece pendente e é registrada; não equivale a dungeon criada. A geração inicial pode causar pausas. Comandos administrativos podem criar duplicatas. Nenhuma colocação foi executada nesta tarefa.

Saves anteriores não recebem colocação automática nem deduplicação das dungeons antigas; a frequência natural também fica zerada nos seus chunks novos. Saves/construções existentes não foram editados pela instalação. Para obter ausência de vestígios antigos das pragas e a fila automática completa, use mundo novo.

## Instalação e validações

Foram instalados **34 arquivos**: Router, 31 JSONs (14 Spore, seis TFTH, 11 Cataclysm), script KubeJS e JAR próprio `m2-worldgen-1.1.0.jar`. Oito arquivos preexistentes ficaram em backup ZIP próprio, reaberto e comparado byte a byte. Backups planos e ignorados em `extras/`; nenhum inclui JAR de terceiros ou save. O JAR mantém o nome/versão históricos: identifique esta compilação pelo SHA-256 do relatório, não apenas pelo nome.

- [Instalação, hashes e backup](instalacao-pragas-cataclysm-20261026.json): 34 destinos idênticos às fontes, nenhum processo Java ativo durante instalação; inclui assinaturas dos oito alvos novos dos mixins.
- [Inventário novo do ZIP e origens](auditoria-pragas-cataclysm-20261026.json): 134 JARs, hashes e licenças declaradas; não certifica compatibilidade ou autorização de redistribuição. ZIP anterior intacto. Spore está somente no perfil M2D e foi identificado separadamente; Cataclysm e TFTH do perfil são idênticos aos do ZIP. Inventários/aprovações históricos preservados.
- [Contrato estático](contrato-pragas-cataclysm-20261026.json): presets normais/toroidal, larguras, Ars e dados gameplay preservados. Java compilado com `--release 21`, integridade ZIP e bytecode 65 conferidos. Aviso de API deprecated em `WorldGenRegion.getLevel`; sem erro de compilação.
- Sintaxe Python/JS, JSON/TOML, destinos exclusivos no Router, 15 IDs sem repetição e coordenadas, 11 frequências zero conferidos. `git diff --check` executado. Site: `npm run build` aprovado, 22 páginas, sem alteração de fontes/visual.

Reprodução estática na raiz:

```powershell
python modpack/development/plagues_cataclysm_data.py --check
python modpack/development/build_integration.py
python modpack/development/audit_worldgen_contract.py --output extras/RELATORIO_NOVO.json
node --check modpack/kubejs/server_scripts/cataclysm_unique.js
```

O gerador aceita `--profile CAMINHO` para a origem Spore e `--audit-output extras/ARQUIVO_NOVO.json` para inventário novo; não sobrescreve auditorias. Não foi renovado o manifesto histórico de WDA nem a aprovação dos servidores anteriores. Aplicação real dos mixins, exploração/propagação e as 15 colocações continuam pendentes em J32–J38 de [TESTES.md](../TESTES.md). Sem commit, push, release ou publicação.
