package br.com.minecraft2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;

/** Complementa o Router antes de predicados de spawn com efeitos colaterais. */
@Mod("m2_worldgen")
public final class WorldgenIntegration {
    public WorldgenIntegration(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(WorldgenIntegration::finalizeSpawn);
        bus.addListener((RegisterEvent event) -> event.register(Registries.BIOME_SOURCE,
            helper -> helper.register(ResourceLocation.parse("m2_worldgen:isolated_climate"),IsolatedClimateBiomeSource.CODEC)));
    }

    public static boolean forbidden(EntityType<?> entity, ResourceLocation dimension, MobSpawnType reason) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return false;
        String namespace = BuiltInRegistries.ENTITY_TYPE.getKey(entity).getNamespace();
        String world = dimension.toString();
        if (namespace.equals("alexsmobs")) {
            return world.equals("m2pokemon:world") || world.equals("m2arcana:world");
        }
        if (namespace.equals("ars_additions") || namespace.equals("ars_structurize") || namespace.equals("ars_fauna")) {
            return !world.equals("m2arcana:world");
        }
        return false;
    }

    private static void finalizeSpawn(FinalizeSpawnEvent event) {
        if (forbidden(event.getEntity().getType(), event.getLevel().getLevel().dimension().location(), event.getSpawnType())) {
            event.setSpawnCancelled(true);
        }
    }
}
