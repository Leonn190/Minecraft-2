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

/**
 * Prioridade maior que o hook Alex's Caves (-69420): mantém as cavernas
 * naturais para todas as posições fora das seis reservas.
 */
@Mixin(value = MultiNoiseBiomeSource.class, priority = 1200)
public abstract class CaveBiomeSourceMixin implements CaveSeedAware {
    @Unique private volatile boolean m2$hasSeed;
    @Unique private volatile boolean m2$isOverworld;
    @Unique private volatile long m2$seed;
    @Unique private volatile List<CaveRegionLayout.Region> m2$layout;

    @Override public void m2$bindSeed(long seed, boolean isOverworld) {
        if (!m2$hasSeed || m2$seed != seed || m2$isOverworld != isOverworld) {
            m2$layout = null;
            m2$seed = seed;
            m2$isOverworld = isOverworld;
            m2$hasSeed = true;
        }
    }

    @Unique private List<CaveRegionLayout.Region> m2$getLayout(Climate.Sampler sampler) {
        List<CaveRegionLayout.Region> result = m2$layout;
        if (result == null) {
            synchronized (this) {
                result = m2$layout;
                if (result == null) {
                    result = CaveRegionLayout.plan(m2$seed, (type, bx, bz) -> {
                        // Climate, profundidade e temperatura reais; sem criar chunks.
                        Climate.TargetPoint climate = sampler.sample(bx >> 2, -8, bz >> 2);
                        float continent = Climate.unquantizeCoord(climate.continentalness());
                        float temp = Climate.unquantizeCoord(climate.temperature());
                        if (type == 3) return continent >= -0.95F && continent <= -0.65F
                            && temp >= -1.0F && temp <= 0.5F;
                        float floor = switch (type) {
                            case 0, 4 -> 0.6F;
                            case 1 -> 0.4F;
                            default -> 0.5F;
                        };
                        return continent >= floor && continent <= 1.0F;
                    });
                    CaveRegionLayout.record(m2$seed, result);
                    m2$layout = result;
                }
            }
        }
        return result;
    }

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void m2$reservedCaves(int x, int y, int z, Climate.Sampler sampler,
                                 CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!m2$hasSeed || !m2$isOverworld) return;
        float depth = Climate.unquantizeCoord(sampler.sample(x, y, z).depth());
        if (depth < 0.2F || depth > 1.5F) return; // não pinta a superfície
        for (CaveRegionLayout.Region region : m2$getLayout(sampler)) {
            if (!region.contains(x << 2, z << 2)) continue;
            ResourceKey<Biome> key = ResourceKey.create(net.minecraft.core.registries.Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("alexscaves", CaveRegionLayout.NAMES[region.type()]));
            Map<ResourceKey<Biome>, Holder<Biome>> map =
                ((BiomeSourceAccessor)(Object)this).getResourceKeyMap();
            Holder<Biome> biome = map.get(key);
            if (biome != null) cir.setReturnValue(biome);
            return;
        }
    }
}
