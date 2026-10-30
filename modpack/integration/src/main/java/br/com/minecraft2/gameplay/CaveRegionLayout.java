package br.com.minecraft2.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seis reservas estáveis na área canônica do toro do Minecraft 2.
 * O planejamento não gera chunks e não depende da ordem de carregamento.
 */
public final class CaveRegionLayout {
    public static final int RADIUS = 160;
    public static final int WORLD_MIN = -4992;
    public static final int WORLD_MAX = 4992; // exclusivo: preset [-312, 312) chunks
    public static final String[] NAMES = {
        "magnetic_caves", "primordial_caves", "toxic_caves",
        "abyssal_chasm", "forlorn_hollows", "candy_cavity"
    };
    private static final int SEPARATION = 960;
    private static final ConcurrentHashMap<Long, List<Region>> ACTIVE = new ConcurrentHashMap<>();

    public record Region(int type, int x, int z) {
        public boolean contains(int bx, int bz) {
            // O toro pode consultar cópias de coordenadas fora do retângulo canônico.
            long dx = fold(bx) - (long) x;
            long dz = fold(bz) - (long) z;
            return dx * dx + dz * dz <= (long) RADIUS * RADIUS;
        }
        public boolean fits() {
            return x - RADIUS >= WORLD_MIN && x + RADIUS < WORLD_MAX
                && z - RADIUS >= WORLD_MIN && z + RADIUS < WORLD_MAX;
        }
    }

    @FunctionalInterface
    public interface ClimatePenalty {
        /** Zero = clima ideal; valores positivos = distância climática. Sempre finito. */
        double at(int caveType, int blockX, int blockZ);
    }

    private CaveRegionLayout() {}

    public static int fold(int block) {
        return (int) (WORLD_MIN + Math.floorMod((long) block - WORLD_MIN,
                                                (long) WORLD_MAX - WORLD_MIN));
    }

    /**
     * Escolhe a melhor candidata para cada bioma, com desempate por seed.
     * Mesmo sem clima ideal encontra uma reserva por tipo, sem alterar a superfície.
     * O fallback garante reservas de BIOMA, não valida sozinho terreno/estruturas.
     */
    public static List<Region> plan(long seed, ClimatePenalty climate) {
        List<int[]> candidates = new ArrayList<>();
        for (int x = -3900; x <= 3900; x += 650) {
            for (int z = -3900; z <= 3900; z += 650) {
                if ((long) x * x + (long) z * z >= 900L * 900L) {
                    candidates.add(new int[]{x, z});
                }
            }
        }
        List<Region> selected = new ArrayList<>(NAMES.length);
        for (int type = 0; type < NAMES.length; type++) {
            List<int[]> ordered = new ArrayList<>(candidates);
            Collections.shuffle(ordered,
                new Random(seed ^ 0x6d325f5e201aL ^ (type * 0x9E3779B97F4A7C15L)));
            Region best = null;
            double bestPenalty = Double.POSITIVE_INFINITY;
            for (int[] at : ordered) {
                Region candidate = new Region(type, at[0], at[1]);
                if (!candidate.fits()) continue;
                boolean farEnough = true;
                for (Region previous : selected) {
                    long dx = candidate.x() - (long) previous.x();
                    long dz = candidate.z() - (long) previous.z();
                    if (dx * dx + dz * dz < (long) SEPARATION * SEPARATION) {
                        farEnough = false;
                        break;
                    }
                }
                if (!farEnough) continue;
                double penalty = climate.at(type, candidate.x(), candidate.z());
                if (!Double.isFinite(penalty) || penalty < 0) {
                    throw new IllegalArgumentException("Penalidade climática inválida: " + penalty);
                }
                if (best == null || penalty < bestPenalty) {
                    best = candidate;
                    bestPenalty = penalty;
                    if (penalty == 0) break; // primeira candidata válida em ordem aleatória estável
                }
            }
            if (best == null) {
                throw new IllegalStateException("Território sem espaço para a reserva " + NAMES[type]);
            }
            selected.add(best);
        }
        return List.copyOf(selected);
    }

    public static int find(List<Region> regions, int blockX, int blockZ) {
        for (Region region : regions) {
            if (region.contains(blockX, blockZ)) return region.type();
        }
        return -1;
    }

    public static void record(long seed, List<Region> regions) {
        if (regions.size() != NAMES.length) {
            throw new IllegalArgumentException("Layout parcial de cavernas");
        }
        ACTIVE.put(seed, List.copyOf(regions));
    }

    /** Pré-filtro dos mapas: somente reservas já calculadas num mundo elegível. */
    public static int at(long seed, int blockX, int blockZ) {
        return find(ACTIVE.getOrDefault(seed, List.of()), blockX, blockZ);
    }
}
