package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mantém fluidos existentes; interrompe somente sua atualização espontânea. */
@Mixin(FluidState.class)
public abstract class FleshFluidMixin {
    @Shadow public abstract Fluid getType();
    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    private void m2$flow(Level level, BlockPos pos, CallbackInfo ci) {
        if (!level.isClientSide && GameplayRules.flesh(BuiltInRegistries.FLUID.getKey(getType()))
                && !GameplayRules.fleshAllowed(level)) ci.cancel();
    }
}
