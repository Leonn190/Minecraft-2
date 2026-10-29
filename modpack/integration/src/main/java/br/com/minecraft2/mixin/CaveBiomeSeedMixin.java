package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.CaveSeedAware;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.Level;
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

/** Mesmo hook generateBiomes que Alex's Caves usa para associar seed ao biome source. */
@Mixin(ChunkStatusTasks.class)
public abstract class CaveBiomeSeedMixin {
    @Inject(method = "generateBiomes", at = @At("HEAD"))
    private static void m2$associateSeed(WorldGenContext context, ChunkStep step,
            StaticCache2D<GenerationChunkHolder> cache, ChunkAccess chunk,
            CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir) {
        if (context.generator().getBiomeSource() instanceof MultiNoiseBiomeSource source) {
            ((CaveSeedAware)(Object)source).m2$bindSeed(context.level().getSeed(),
                context.level().dimension() == Level.OVERWORLD);
        }
    }
}
