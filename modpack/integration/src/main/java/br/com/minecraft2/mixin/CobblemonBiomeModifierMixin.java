package br.com.minecraft2.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.cobblemon.mod.neoforge.worldgen.CobblemonBiomeModifiers", remap = false)
public abstract class CobblemonBiomeModifierMixin {
    // O injetor coded ignora dimensão e algumas features nem exigem tag.
    // O Pokémon já usa biomas clonados exclusivos; conserva a seleção de tags do mod.
    @Inject(method = "modify", at = @At("HEAD"), cancellable = true, remap = false)
    private void m2$onlyPokemonBiomes(Holder<Biome> biome, BiomeModifier.Phase phase,
            ModifiableBiomeInfo.BiomeInfo.Builder builder, CallbackInfo callback) {
        if (!biome.unwrapKey().map(key -> key.location().getNamespace().equals("m2pokemon")).orElse(false)) {
            callback.cancel();
        }
    }
}
