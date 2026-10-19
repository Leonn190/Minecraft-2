scoreboard players enable @a m2p_enter
scoreboard players enable @a m2p_leave
execute as @a[scores={m2p_enter=1..}] at @s if dimension minecraft:overworld run function m2pokemon:enter
execute as @a[scores={m2p_leave=1..}] at @s if dimension m2pokemon:world run function m2pokemon:leave
scoreboard players set @a[scores={m2p_enter=1..}] m2p_enter 0
scoreboard players set @a[scores={m2p_leave=1..}] m2p_leave 0
