package br.com.minecraft2.mixin;

import br.com.minecraft2.WorldgenIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {
    // Ars Fauna pode invocar MOB_SUMMONED dentro do próprio predicado natural.
    // A interrupção precisa ocorrer antes desse predicado, não só após a criação.
    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void m2$beforePredicate(EntityType<?> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> result) {
        if (WorldgenIntegration.forbidden(type, level.getLevel().dimension().location(), reason)) {
            result.setReturnValue(false);
        }
    }
}
