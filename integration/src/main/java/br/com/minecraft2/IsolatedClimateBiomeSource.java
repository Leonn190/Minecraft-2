package br.com.minecraft2;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

/** Mesma seleção climática vanilla, sem herdar as regiões globais do TerraBlender. */
public final class IsolatedClimateBiomeSource extends BiomeSource {
    public static final MapCodec<IsolatedClimateBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(Climate.ParameterList.codec(Biome.CODEC.fieldOf("biome"))
            .fieldOf("biomes").forGetter(source -> source.parameters))
        .apply(instance, IsolatedClimateBiomeSource::new));
    private final Climate.ParameterList<Holder<Biome>> parameters;

    public IsolatedClimateBiomeSource(Climate.ParameterList<Holder<Biome>> parameters) {
        this.parameters=parameters;
    }

    @Override protected MapCodec<? extends BiomeSource> codec() { return CODEC; }
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return parameters.values().stream().map(Pair::getSecond).distinct();
    }
    @Override public Holder<Biome> getNoiseBiome(int x,int y,int z,Climate.Sampler sampler) {
        return parameters.findValue(sampler.sample(x,y,z));
    }
}
