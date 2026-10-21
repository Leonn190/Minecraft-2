package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class FleshBlockTicksMixin {
    @Shadow public abstract Block getBlock();
    @Inject(method={"tick", "randomTick"}, at=@At("HEAD"), cancellable=true)
    private void m2$ticks(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (GameplayRules.flesh(BuiltInRegistries.BLOCK.getKey(getBlock())) && !GameplayRules.fleshAllowed(level)) ci.cancel();
    }
    @Inject(method="entityInside", at=@At("HEAD"), cancellable=true)
    private void m2$contact(Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!level.isClientSide && GameplayRules.flesh(BuiltInRegistries.BLOCK.getKey(getBlock()))
                && !GameplayRules.fleshAllowed(level)) ci.cancel();
    }
}
