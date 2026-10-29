package br.com.minecraft2.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reserva leve e determinística: apenas centros e raios, sem gerar chunks.
 * Uma condição climática REAL é avaliada na fonte de biomas do Overworld.
 */
public final class CaveRegionLayout {
    public static final int RADIUS = 160;
    public static final int WORLD_MIN = -4992;
    public static final int WORLD_MAX = 4992;
    public static final String[] NAMES = {
        "magnetic_caves", "primordial_caves", "toxic_caves",
        "abyssal_chasm", "forlorn_hollows", "candy_cavity"
    };
    private static final int SEPARATION = 960;
    private static final ConcurrentHashMap<Long, List<Region>> ACTIVE = new ConcurrentHashMap<>();

    public record Region(int type, int x, int z) {
        public boolean contains(int bx, int bz) {
            long dx = bx - (long)x, dz = bz - (long)z;
            return dx * dx + dz * dz <= (long)RADIUS * RADIUS;
        }
        public boolean fits() {
            return x - RADIUS >= WORLD_MIN && x + RADIUS <= WORLD_MAX
                && z - RADIUS >= WORLD_MIN && z + RADIUS <= WORLD_MAX;
        }
    }

    @FunctionalInterface
    public interface ClimateFilter {
        boolean matches(int caveType, int blockX, int blockZ);
    }

    private CaveRegionLayout() {}

    /**
     * A ordem das candidatas é embaralhada pela seed, nunca pelo tick/chunk.
     * Não usa saves nem depende da ordem de carregamento dos chunks.
     */
    public static List<Region> plan(long seed, ClimateFilter climate) {
        List<int[]> candidates = new ArrayList<>();
        for (int x = -3900; x <= 3900; x += 650) {
            for (int z = -3900; z <= 3900; z += 650) {
                if ((long)x*x+(long)z*z >= 900L*900L) candidates.add(new int[]{x,z});
            }
        }
        List<Region> selected = new ArrayList<>(NAMES.length);
        for (int type = 0; type < NAMES.length; type++) {
            List<int[]> ordered = new ArrayList<>(candidates);
            Collections.shuffle(ordered, new Random(seed ^ 0x6d325f5e201aL ^ (type * 0x9E3779B97F4A7C15L)));
            for (int[] at : ordered) {
                Region r = new Region(type, at[0], at[1]);
                if (!r.fits() || !climate.matches(type, r.x(), r.z())) continue;
                boolean distant = true;
                for (Region previous : selected) {
                    long dx = r.x() - (long)previous.x(), dz = r.z() - (long)previous.z();
                    if (dx*dx + dz*dz < (long)SEPARATION*SEPARATION) { distant = false; break; }
                }
                if (distant) {
                    selected.add(r);
                    break;
                }
            }
        }
        return List.copyOf(selected);
    }

    public static void record(long seed, List<Region> regions) {
        ACTIVE.put(seed, List.copyOf(regions));
    }

    public static int at(long seed, int blockX, int blockZ) {
        for (Region r : ACTIVE.getOrDefault(seed, List.of())) {
            if (r.contains(blockX, blockZ)) return r.type();
        }
        return -1;
    }
}
