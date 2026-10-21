package br.com.minecraft2.test;

import br.com.minecraft2.gameplay.GameplayRules;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Testes de servidor: eventos reais, registros carregados e mixins aplicados. */
public final class GameplaySmokeTest {
    public static JsonObject audit(MinecraftServer server, JsonArray errors) throws Exception {
        JsonObject result = new JsonObject();
        int checks = 0;
        var fleshType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("the_flesh_that_hates:flesh_human"));
        var spreadClass = Class.forName("net.mcreator.thefleshthathates.FleshBlockSpread");
        for (var level : server.getAllLevels()) {
            boolean allowed = GameplayRules.fleshAllowed(level);
            for (var reason : new MobSpawnType[]{MobSpawnType.NATURAL, MobSpawnType.CHUNK_GENERATION, MobSpawnType.SPAWNER, MobSpawnType.MOB_SUMMONED}) {
                var entity = fleshType.spawn(level, new BlockPos(0, 180, 0), reason);
                boolean added = entity != null && entity.isAddedToLevel();
                if (added != allowed) errors.add("TFTH spawn: "+level.dimension().location()+"/"+reason+"="+added);
                if (entity != null) entity.discard();
                checks++;
            }
            Object spread = spreadClass.getConstructor(net.minecraft.server.level.ServerLevel.class, BlockPos.class).newInstance(level, new BlockPos(0, 180, 0));
            var saved = (net.minecraft.nbt.CompoundTag)spreadClass.getMethod("saveStateToNBT").invoke(spread);
            if (!saved.getString("m2_dimension").equals(level.dimension().location().toString())) errors.add("Dimensão de fila não salva");
            Object restored = spreadClass.getConstructor(net.minecraft.server.level.ServerLevel.class, BlockPos.class).newInstance(null, BlockPos.ZERO);
            spreadClass.getMethod("loadStateFromNBT", net.minecraft.nbt.CompoundTag.class).invoke(restored, saved);
            spreadClass.getMethod("setLevel", net.minecraft.server.level.ServerLevel.class).invoke(restored, server.overworld());
            var field = spreadClass.getDeclaredField("level"); field.setAccessible(true);
            if (field.get(restored) != level) errors.add("Fila restaurada em outra dimensão");
            if (!allowed) {
                var before = spreadClass.getMethod("saveStateToNBT").invoke(restored);
                spreadClass.getMethod("tick").invoke(restored);
                if (!before.equals(spreadClass.getMethod("saveStateToNBT").invoke(restored))) errors.add("Fila proibida continuou processando");
            }
            checks += 2;
            var attacker = EntityType.ZOMBIE.create(level);
            var victim = EntityType.PIG.create(level);
            for (String weapon : new String[]{"minecraft:diamond_sword", "minecraft:netherite_axe", "undergarden:cloggrum_sword", "the_flesh_that_hates:flesh_sword", "minecraft:air"}) {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(weapon));
                if (!weapon.equals("minecraft:air") && item == Items.AIR) throw new IllegalStateException("Item teste ausente: "+weapon);
                attacker.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
                var source = level.damageSources().mobAttack(attacker);
                var event = new LivingIncomingDamageEvent(victim, new DamageContainer(source, 20F));
                NeoForge.EVENT_BUS.post(event);
                float expected = GameplayRules.adjusted(20F, level.dimension().location().toString(), item == Items.AIR ? null : ResourceLocation.parse(weapon).getNamespace());
                if (Math.abs(event.getAmount()-expected) > 0.001) errors.add("Dano "+weapon+" em "+level.dimension().location()+": "+event.getAmount());
                checks++;
            }
            // Ter uma arma na mão não transforma espinhos/dano ambiental em golpe armado.
            for (var source : new DamageSource[]{level.damageSources().fall(), level.damageSources().thorns(attacker)}) {
                attacker.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                var event = new LivingIncomingDamageEvent(victim, new DamageContainer(source, 20F));
                NeoForge.EVENT_BUS.post(event);
                if (event.getAmount() != 20F) errors.add("Dano sem arma alterado: "+source);
                checks++;
            }
            for (var weapon : new net.minecraft.world.item.Item[]{Items.BOW, Items.CROSSBOW, Items.TRIDENT}) {
                var stack = new ItemStack(weapon);
                net.minecraft.world.entity.projectile.AbstractArrow projectile = weapon == Items.TRIDENT
                    ? new net.minecraft.world.entity.projectile.ThrownTrident(level, attacker, stack)
                    : new net.minecraft.world.entity.projectile.Arrow(level, attacker, new ItemStack(Items.ARROW), stack);
                attacker.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                DamageSource source = weapon == Items.TRIDENT ? level.damageSources().trident(projectile, attacker)
                    : level.damageSources().arrow(projectile, attacker);
                var event = new LivingIncomingDamageEvent(victim, new DamageContainer(source, 20F));
                NeoForge.EVENT_BUS.post(event);
                if (event.getAmount() != GameplayRules.adjusted(20F, level.dimension().location().toString(), "minecraft")) errors.add("Projétil perdeu arma de origem: "+weapon);
                checks++;
            }
            attacker.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.APPLE));
            var appleEvent = new LivingIncomingDamageEvent(victim, new DamageContainer(level.damageSources().mobAttack(attacker), 20F));
            NeoForge.EVENT_BUS.post(appleEvent);
            if (appleEvent.getAmount() != 20F) errors.add("Item sem atributo de arma tratado como arma");
            checks++;
        }
        result.addProperty("checks", checks);
        result.addProperty("scope", "Servidor dedicado, eventos reais e persistência de filas; sem cliente ou jogadores conectados.");
        return result;
    }
}
