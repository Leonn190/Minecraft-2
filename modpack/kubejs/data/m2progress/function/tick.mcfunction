scoreboard players enable @a m2a_enter
scoreboard players enable @a m2a_leave
execute as @a at @s if dimension m2pokemon:world unless entity @s[tag=m2p_unlocked] run function m2progress:unlock_pokemon
execute as @a at @s if dimension m2arcana:world unless entity @s[tag=m2a_unlocked] run function m2progress:unlock_arcana
execute as @a[scores={m2a_enter=1..}] at @s if dimension minecraft:overworld run function m2progress:enter_arcana
execute as @a[scores={m2a_leave=1..}] at @s if dimension m2arcana:world run function m2progress:leave_arcana
scoreboard players set @a[scores={m2a_enter=1..}] m2a_enter 0
scoreboard players set @a[scores={m2a_leave=1..}] m2a_leave 0
