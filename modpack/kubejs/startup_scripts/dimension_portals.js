// BBL Portals 1.2.2. Exige reinício completo e a integração Java m2_worldgen.
const M2PortalIntegration = Java.loadClass('br.com.minecraft2.portal.PortalIntegration')

PortalEvents.register(event => {
  // Ambos os materiais apontam ao mesmo destino: qualquer lado interno pode acender.
  // A integração exige ferro embaixo, redstone em cima e todos os cantos corretos.
  ;['minecraft:iron_block', 'minecraft:redstone_block'].forEach(frame => {
    const portal = event.create()
      .frameBlock(frame)
      .lightWithItem('undergarden:catalyst')
      .setDestination('m2pokemon:world')
      .returnDim('minecraft:overworld', true)
      .tint(220, 35, 45)
      .showInJEI(false) // O desenho uniforme do JEI não representa esta moldura composta.
    M2PortalIntegration.configure(portal, true)
  })
  const arcana = event.create()
    .frameBlock('minecraft:crying_obsidian')
    .lightWithItem('undergarden:catalyst')
    .setDestination('m2arcana:world')
    .returnDim('minecraft:overworld', true)
    .tint(155, 60, 230)
  M2PortalIntegration.configure(arcana, false)
})
