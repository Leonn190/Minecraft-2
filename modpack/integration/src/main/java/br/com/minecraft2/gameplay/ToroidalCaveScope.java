package br.com.minecraft2.gameplay;

import com.exoticworlds.engine.gen.LoopedChunkGenerator;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Requer exatamente o gerador e os limites do preset m2worlds:toroidal. */
public final class ToroidalCaveScope {
    private ToroidalCaveScope() {}

    public static boolean enabled(ChunkGenerator generator, ResourceKey<Level> dimension) {
        if (!Level.OVERWORLD.equals(dimension)
                || !(generator instanceof LoopedChunkGenerator looped)) return false;
        var fold = looped.fold();
        var x = fold.chunkDomain(Direction.Axis.X);
        var z = fold.chunkDomain(Direction.Axis.Z);
        return fold.isWrapped()
            && x.loops() && z.loops()
            && x.lowerBound == -312 && x.upperBound == 312
            && z.lowerBound == -312 && z.upperBound == 312;
    }
}
