package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.CaveRegionLayout;
import br.com.minecraft2.gameplay.CaveSeedAware;
import com.github.alexmodguy.alexscaves.server.level.biome.BiomeSourceAccessor;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reserva biomas oficiais do Alex's Caves, sem reimplementar as features. */
@Mixin(value = MultiNoiseBiomeSource.class, priority = 1200)
public abstract class CaveBiomeSourceMixin implements CaveSeedAware {
    @Unique private volatile boolean m2$hasSeed;
    @Unique private volatile boolean m2$isTargetWorld;
    @Unique private volatile long m2$seed;
    @Unique private volatile List<CaveRegionLayout.Region> m2$layout;

    @Override
    public synchronized void m2$bindSeed(long seed, boolean isTargetWorld) {
        if (!m2$hasSeed || m2$seed != seed || m2$isTargetWorld != isTargetWorld) {
            m2$hasSeed = false;
            m2$layout = null;
            m2$seed = seed;
            m2$isTargetWorld = isTargetWorld;
            m2$hasSeed = true;
        }
    }

    @Unique private static double m2$outside(float value, double min, double max) {
        return Math.max(0.0, Math.max(min - value, value - max));
    }

    @Unique private static double m2$climatePenalty(int type, int bx, int bz,
                                                       Climate.Sampler sampler) {
        // Os intervalos seguem modpack/config/alexscaves_biome_generation/*.json,
        // não os defaults do Alex's Caves. Amostragem subterrânea em quart coords.
        Climate.TargetPoint point = sampler.sample(bx >> 2, -8, bz >> 2);
        float continent = Climate.unquantizeCoord(point.continentalness());
        if (type == 3) {
            float temperature = Climate.unquantizeCoord(point.temperature());
            return m2$outside(continent, -0.95, -0.65)
                + m2$outside(temperature, -1.0, 0.5);
        }
        double min = switch (type) {
            case 0, 4 -> -1.0;
            case 1 -> 0.4;
            default -> 0.5;
        };
        return m2$outside(continent, min, 1.0);
    }

    @Unique private List<CaveRegionLayout.Region> m2$getLayout(Climate.Sampler sampler) {
        List<CaveRegionLayout.Region> result = m2$layout;
        if (result != null) return result;
        synchronized (this) {
            result = m2$layout;
            if (result == null) {
                result = CaveRegionLayout.plan(m2$seed,
                    (type, bx, bz) -> m2$climatePenalty(type, bx, bz, sampler));
                CaveRegionLayout.record(m2$seed, result);
                m2$layout = result;
            }
        }
        return result;
    }

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void m2$reservedCaves(int x, int y, int z, Climate.Sampler sampler,
                                  CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!m2$hasSeed || !m2$isTargetWorld) return;
        int type = CaveRegionLayout.find(m2$getLayout(sampler), x << 2, z << 2);
        if (type < 0) return; // SEM amostragem climática para posições fora da reserva

        // Profundidade do modpack (0.05..2.0), não dos defaults upstream.
        float depth = Climate.unquantizeCoord(sampler.sample(x, y, z).depth());
        if (depth < 0.05F || depth > 2.0F) return;
        ResourceKey<Biome> key = ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath("alexscaves", CaveRegionLayout.NAMES[type]));
        Map<ResourceKey<Biome>, Holder<Biome>> map =
            ((BiomeSourceAccessor) (Object) this).getResourceKeyMap();
        Holder<Biome> biome = map.get(key);
        if (biome != null) cir.setReturnValue(biome);
        // Se o holder ainda não estiver registrado, não forçar null.
    }
}
