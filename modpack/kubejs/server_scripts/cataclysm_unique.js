// M2-029: uma de cada estrutura por SAVE NOVO. Saves antigos ficam inalterados.
// O executor Java recusa áreas já carregadas/salvas e mantém estado persistente.
// Intervalo: 200 ticks por tentativa. Consulte [M2 CATACLYSM] no log.
const M2Cataclysm = Java.loadClass('br.com.minecraft2.gameplay.UniqueCataclysm')
const m2CataclysmStructures = [
  ['abandoned_spire', 'minecraft:overworld', -2256, -2256],
  ['abandoned_temple', 'minecraft:overworld', -752, -2256],
  ['abandoned_village', 'minecraft:overworld', 752, -2256],
  ['acropolis', 'minecraft:overworld', 2256, -2256],
  ['amethyst_nest', 'minecraft:overworld', -2256, -752],
  ['ancient_factory', 'minecraft:overworld', -752, -752],
  ['cursed_pyramid', 'minecraft:overworld', 752, -752],
  ['desert_occupied_village', 'minecraft:overworld', 2256, -752],
  ['desert_site', 'minecraft:overworld', -2256, 752],
  ['desert_temple', 'minecraft:overworld', -752, 752],
  ['frosted_prison', 'minecraft:overworld', 752, 752],
  ['sunken_city', 'minecraft:overworld', 2256, 752],
  ['burning_arena', 'minecraft:the_nether', -1200, 0],
  ['soul_black_smith', 'minecraft:the_nether', 1200, 0],
  ['ruined_citadel', 'minecraft:the_end', 1600, 0]
]
let m2CataclysmCursor = 0
ServerEvents.tick(event => {
  if (event.server.getTickCount() % 200 !== 0 || m2CataclysmCursor >= m2CataclysmStructures.length) return
  const entry = m2CataclysmStructures[m2CataclysmCursor]
  if (M2Cataclysm.placeOnce(event.server, entry[0], entry[1], entry[2], entry[3])) m2CataclysmCursor++
})
