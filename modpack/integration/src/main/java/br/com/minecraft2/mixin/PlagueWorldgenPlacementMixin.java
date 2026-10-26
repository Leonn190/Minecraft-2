package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class PlagueWorldgenPlacementMixin {
    @Inject(method="setBlock", at=@At("HEAD"), cancellable=true)
    private void m2$block(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if (GameplayRules.plagueForbidden(BuiltInRegistries.BLOCK.getKey(state.getBlock()), ((WorldGenRegion)(Object)this).getLevel())) cir.setReturnValue(false);
    }
}
