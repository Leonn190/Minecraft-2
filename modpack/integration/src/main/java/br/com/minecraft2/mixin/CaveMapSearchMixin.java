package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.CaveRegionLayout;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** O mapa do Alex's Caves checa este predicado ANTES de consultar o bioma. */
@Mixin(value = ACBiomeRarity.class, remap = false)
public abstract class CaveMapSearchMixin {
    @Inject(method = "isQuartInRareBiome", at = @At("HEAD"), cancellable = true, remap = false)
    private static void m2$includeReserved(long seed, int quartX, int quartZ,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (CaveRegionLayout.at(seed, quartX << 2, quartZ << 2) >= 0) cir.setReturnValue(true);
    }
}
