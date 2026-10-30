package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.CaveSeedAware;
import br.com.minecraft2.gameplay.ToroidalCaveScope;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/** Ativa as reservas somente no Overworld toroidal de dimensões M2. */
@Mixin(ChunkStatusTasks.class)
public abstract class CaveBiomeSeedMixin {
    @Inject(method = "generateBiomes", at = @At("HEAD"))
    private static void m2$associateSeed(WorldGenContext context, ChunkStep step,
            StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if (context.generator().getBiomeSource() instanceof MultiNoiseBiomeSource source) {
            boolean target = ToroidalCaveScope.enabled(
                context.generator(), context.level().dimension());
            ((CaveSeedAware) (Object) source).m2$bindSeed(
                context.level().getSeed(), target);
        }
    }
}
