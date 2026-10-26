package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fecha também colocações por itens, conversões, templates e comandos. */
@Mixin(Level.class)
public abstract class PlaguePlacementMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at=@At("HEAD"), cancellable=true)
    private void m2$block(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level)(Object)this;
        if (!level.isClientSide && GameplayRules.plagueForbidden(BuiltInRegistries.BLOCK.getKey(state.getBlock()), level)) cir.setReturnValue(false);
    }
}
