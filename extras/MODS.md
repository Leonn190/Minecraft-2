# Mods — candidatos, não lista definitiva

**Minecraft Java 1.21.1 / NeoForge.** Esta lista é um ponto de planejamento, **não é confirmação** de compatibilidade ou de inclusão no pacote.

O inventário local do ZIP foi auditado em 21/10/2026: consulte [`modpack/mods-list.txt`](../modpack/mods-list.txt), o [manifesto com hashes](../modpack/mods-manifest.json) e o [relatório de validação](VALIDACAO_WORLDGEN.md). A tabela de candidatos abaixo permanece como planejamento.

| Área | Mods candidatos | Status |
|---|---|---|
| Engenharia | Create e addons | Avaliar versões e dependências |
| Magia | Ars Nouveau | Avaliar versão, integração e balanceamento |
| Aventura | Alex's Caves, Cataclysm | Validar loader e compatibilidade |
| Dimensões | The Undergarden, The Midnight, Deeper and Darker | Validar disponibilidade para 1.21.1 NeoForge |
| Qualidade de vida | JEI, Jade, Sophisticated Backpacks, Paraglider | Validar versões |
| Comida | Farmer's Delight, Supplementaries | Validar versões |
| Personalização | KubeJS | Validar eventos/APIs usados |
| Otimização | ModernFix, Entity Culling | Validar versões e conflitos |
| Possíveis extensões | Cobblemon, Box of Horrors | Avaliar separadamente; não prometer compatibilidade |

## Antes de adicionar um mod

1. Confirmar nome, link oficial, versão exata, loader e dependências.
2. Verificar se a licença permite a forma pretendida de distribuição.
3. Testar numa instância NeoForge 1.21.1 isolada.
4. Documentar conflitos ou alterações necessárias.
5. Registrar versão aprovada e origem em uma tabela de dependências.

## Distribuição

A exportação CurseForge usa um ZIP com `manifest.json` na raiz e uma pasta `overrides/`; não publique uma pasta `mods/` cheia de JARs de terceiros por conveniência. Consulte as regras oficiais de exportação e redistribuição.
