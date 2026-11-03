// M2-035: copiar SOMENTE para kubejs/server_scripts de um cliente descartável.
// Requer m2-disposable-client.json, mundo com este nome e JAR camera-tests.
// /reload inicia visitas reais em espectador, auditoria de câmera e 63 aliases.
// Não copiar para o perfil M2D. Não faz parte do JAR de produção.
const m2CameraApi = Java.loadClass('com.exoticworlds.api.v1.ExoticWorldsApi')
const m2CameraAxis = Java.loadClass('net.minecraft.core.Direction$Axis')
const m2CameraObjects = Java.loadClass('java.util.Objects')
const m2CameraDimensions = [
  ['minecraft:overworld', 9984], ['minecraft:the_nether', 4992],
  ['minecraft:the_end', 6016], ['m2pokemon:world', 4992],
  ['m2arcana:world', 3008], ['undergarden:undergarden', 4992], ['macabre:the_pit', 2016]
]
let m2CameraStep = 0
let m2CameraNext = 0
let m2CameraDone = false
ServerEvents.tick(event => {
  const server = event.server
  if (m2CameraDone || server.getCommands().getDispatcher().getRoot().getChild('m2cameraaudit') === null) return
  if (server.getWorldData().getLevelName() !== 'M2-035 Toroidal Creative') return
  const player = server.getPlayerList().getPlayerByName('M2Audit')
  if (player === null || server.getTickCount() < m2CameraNext) return
  m2CameraNext = server.getTickCount() + 200
  if (m2CameraStep === 0) server.runCommandSilent('gamemode spectator M2Audit')
  if (m2CameraStep >= m2CameraDimensions.length) {
    server.runCommandSilent('execute in minecraft:overworld run tp M2Audit 0 160 0')
    server.runCommandSilent('gamemode creative M2Audit')
    console.info('[M2 CAMERA SCENARIO] completed')
    m2CameraDone = true
    return
  }
  const entry = m2CameraDimensions[m2CameraStep++]
  const report = { dimension: entry[0], width: entry[1], aliases: 0, passed: false }
  try {
    server.runCommandSilent('execute in ' + entry[0] + ' run tp M2Audit 0 150 0')
    var level = player.serverLevel()
    var shape = m2CameraApi.shapeOf(level).orElseThrow()
    if (shape.widthBlocks(m2CameraAxis.X) !== entry[1] || shape.widthBlocks(m2CameraAxis.Z) !== entry[1]) throw new Error('largura divergente')
    var original = level.getChunk(0, 0)
    var width = entry[1] / 16
    for (let x = -1; x <= 1; x++) {
      for (let z = -1; z <= 1; z++) {
        if (!m2CameraObjects.equals(original, level.getChunk(x * width, z * width))) throw new Error('chunk alias divergente: ' + x + ',' + z)
        report.aliases++
      }
    }
    server.runCommandSilent('execute as M2Audit run m2cameraaudit')
    report.passed = String(level.dimension) === entry[0]
  } catch (error) { report.error = String(error) }
  console.info('[M2 CAMERA SCENARIO] ' + JSON.stringify(report))
})
