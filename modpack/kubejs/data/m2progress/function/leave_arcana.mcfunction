effect give @s minecraft:resistance 12 4 true
execute in minecraft:overworld positioned 0 125 0 run spreadplayers 0 0 4 55 false @s
tellraw @s {"text":"[Minecraft 2] Retorno ao Overworld.","color":"green"}
