package br.com.minecraft2.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Move a tabela configurada upstream para os clones exclusivos Pokémon. */
@Pseudo
@Mixin(targets="com.Harbinger.Spore.ExtremelySusThings.BiomeModification", remap=false)
public abstract class SporeBiomeModifierMixin {
    @Shadow private void addSpawns(ModifiableBiomeInfo.BiomeInfo.Builder builder, int bonus) { throw new AssertionError(); }
    @Inject(method="modify", at=@At("HEAD"), cancellable=true)
    private void m2$biomes(Holder<Biome> biome, BiomeModifier.Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder, CallbackInfo ci) {
        var key = biome.unwrapKey();
        if (phase == BiomeModifier.Phase.ADD && key.isPresent() && key.get().location().getNamespace().equals("m2pokemon"))
            addSpawns(builder, key.get().location().getPath().equals("minecraft/mushroom_fields") ? 20 : 0);
        ci.cancel();
    }
}
