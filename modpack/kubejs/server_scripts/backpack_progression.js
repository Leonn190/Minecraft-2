// M2-031: aparências intencionais; não ordenar pelos metais vanilla.
ServerEvents.recipes(event => {
  const namespace = 'sophisticatedbackpacks:'
  const stages = [
    ['netherite_backpack', 'backpack', 'cloggrum_ingot', 8],
    ['iron_backpack', 'netherite_backpack', 'froststeel_ingot', 8],
    ['copper_backpack', 'iron_backpack', 'utherium_crystal', 8],
    ['gold_backpack', 'copper_backpack', 'regalium_crystal', 4],
    ['diamond_backpack', 'gold_backpack', 'forgotten_ingot', 4]
  ]
  // Remove por resultado: inclui crafting alternativo e smithing Netherite.
  stages.forEach(stage => event.remove({ output: namespace + stage[0] }))
  stages.forEach(stage => {
    event.custom({
      type: 'm2_worldgen:backpack_progression',
      category: 'misc',
      key: {
        B: { item: namespace + stage[1] },
        M: { item: 'undergarden:' + stage[2] }
      },
      pattern: stage[3] === 8 ? ['MMM', 'MBM', 'MMM'] : [' M ', 'MBM', ' M '],
      result: { id: namespace + stage[0], count: 1 }
    }).id('m2progress:backpacks/' + stage[0])
  })
  // Leather e upgrades internos do mod permanecem intactos.
})
