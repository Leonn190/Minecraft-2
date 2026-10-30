package br.com.minecraft2.test;

import br.com.minecraft2.gameplay.CaveRegionLayout;
import java.util.HashSet;
import java.util.List;

/** Testes isolados: não representam simulação de chunks do Minecraft. */
public final class CaveRegionLayoutTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertLayout(List<CaveRegionLayout.Region> regions) {
        check(regions.size() == 6, "Não foram reservadas seis cavernas");
        HashSet<Integer> types = new HashSet<>();
        for (var r : regions) {
            check(types.add(r.type()), "Tipo repetido");
            check(r.fits(), "Fora do toro");
            check((long) r.x() * r.x() + (long) r.z() * r.z() >= 900L * 900L, "Spawn");
            check(CaveRegionLayout.find(regions, r.x(), r.z()) == r.type(), "Lookup");
            check(r.contains(r.x() + 9984, r.z()), "Dobra X");
            check(r.contains(r.x(), r.z() - 9984), "Dobra Z");
            for (var other : regions) {
                if (r == other) continue;
                long dx = r.x() - (long) other.x();
                long dz = r.z() - (long) other.z();
                check(dx * dx + dz * dz >= 960L * 960L, "Reservas sobrepostas");
            }
        }
        check(types.size() == 6, "Faltou tipo");
    }

    public static void main(String[] args) {
        for (long seed = -100; seed <= 100; seed++) {
            var a = CaveRegionLayout.plan(seed, (type, x, z) -> 0.0);
            var b = CaveRegionLayout.plan(seed, (type, x, z) -> 0.0);
            check(a.equals(b), "Não determinístico: " + seed);
            assertLayout(a);
            // Todos os pontos climaticamente ruins: o fallback nunca abandona um tipo.
            var fallback = CaveRegionLayout.plan(seed, (type, x, z) -> 100.0);
            assertLayout(fallback);
            // Clima ideal apenas no semiplano X>=0: preferir locais válidos.
            var preferred = CaveRegionLayout.plan(seed,
                (type, x, z) -> x >= 0 ? 0.0 : 5.0);
            assertLayout(preferred);
            for (var r : preferred) {
                check(r.x() >= 0, "Ignorou candidato climático válido");
            }
        }
        check(CaveRegionLayout.fold(4992) == -4992, "Extremo superior exclusivo");
        check(CaveRegionLayout.fold(-4993) == 4991, "Extremo inferior");
        check(CaveRegionLayout.fold(4992 + 9984) == -4992, "Duas voltas");
        System.out.println("PASS: 201 seeds; 6 reservas; fallback; preferência; dobra toroidal");
    }
}
