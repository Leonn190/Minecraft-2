package br.com.minecraft2.gameplay;

/** Semente apenas do Overworld: outros biomesources permanecem intactos. */
public interface CaveSeedAware {
    void m2$bindSeed(long seed, boolean isOverworld);
}
