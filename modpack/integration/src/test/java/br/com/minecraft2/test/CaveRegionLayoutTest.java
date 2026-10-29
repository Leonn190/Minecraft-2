package br.com.minecraft2.test;

import br.com.minecraft2.gameplay.CaveRegionLayout;
import java.util.List;

/** Testes isolados de layout, sem substituir testes de geração real. */
public final class CaveRegionLayoutTest {
    public static void main(String[] args) {
        for (long seed = -100; seed <= 100; seed++) {
            List<CaveRegionLayout.Region> a = CaveRegionLayout.plan(seed, (type, x, z) -> true);
            List<CaveRegionLayout.Region> b = CaveRegionLayout.plan(seed, (type, x, z) -> true);
            if (!a.equals(b) || a.size() != 6) throw new AssertionError("Determinismo/cobertura seed " + seed);
            for (var r : a) {
                if (!r.fits()) throw new AssertionError("Fora dos limites");
                if (r.x()*r.x()+r.z()*r.z() < 900*900) throw new AssertionError("Spawn");
                for (var other : a) {
                    if (r == other) continue;
                    long dx = r.x()-(long)other.x(), dz = r.z()-(long)other.z();
                    if (dx*dx+dz*dz < 960L*960L) throw new AssertionError("Sobreposição");
                }
            }
        }
        // Sem clima permitido, a reserva deve recusar, e NÃO inventar um oceano.
        if (!CaveRegionLayout.plan(0, (type,x,z) -> false).isEmpty())
            throw new AssertionError("Filtro climático ignorado");
        System.out.println("PASS CaveRegionLayoutTest (201 seeds, 6 reservas quando clima válido)");
    }
}
