package br.com.minecraft2.gameplay;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/** Geração única somente para saves novos e áreas sem chunks carregados/salvos. */
public final class UniqueCataclysm {
    private static final Set<MinecraftServer> NEW = Collections.newSetFromMap(new WeakHashMap<>());
    private UniqueCataclysm() {}

    public static void beforeStart(ServerAboutToStartEvent event) {
        var root = event.getServer().getWorldPath(LevelResource.ROOT);
        // A tela de criação pode gravar level.dat antes do boot; ausência de chunks
        // é o critério conservador. Qualquer região anterior impede ativação.
        if (event.getServer().getWorldData().overworldData().getGameTime() != 0) return;
        try (var files = Files.walk(root)) {
            if (files.noneMatch(path -> path.getFileName().toString().endsWith(".mca"))) NEW.add(event.getServer());
        } catch (java.io.IOException error) {
            LogUtils.getLogger().warn("[M2 CATACLYSM] Não foi possível confirmar mundo novo; geração automática desativada.");
        }
    }

    public static final class State extends SavedData {
        private final CompoundTag entries;
        private boolean enabled;
        public State() { entries = new CompoundTag(); }
        private State(CompoundTag tag, HolderLookup.Provider registries) {
            entries = tag.getCompound("entries"); enabled = tag.getBoolean("enabled");
        }
        @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.put("entries", entries); tag.putBoolean("enabled", enabled); return tag;
        }
    }

    /** Recusa até regiões parcialmente salvas, sem carregar o chunk para verificá-lo. */
    private static boolean untouched(ServerLevel level, int x, int z) {
        if (level.getChunkSource().hasChunk(x, z)) return false;
        var root = DimensionType.getStorageFolder(level.dimension(), level.getServer().getWorldPath(LevelResource.ROOT));
        return !Files.exists(root.resolve("region/r." + Math.floorDiv(x, 32) + "." + Math.floorDiv(z, 32) + ".mca"));
    }

    public static boolean placeOnce(MinecraftServer server, String name, String dimension, int centerX, int centerZ) {
        var data = server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> {
            var state = new State(); state.enabled = NEW.contains(server); state.setDirty(); return state;
        }, State::new), "m2_unique_cataclysm");
        // Nunca modifica saves preexistentes, mesmo que ainda não tenham nosso SavedData.
        if (!data.enabled) return true;
        var entry = data.entries.getCompound(name);
        if (entry.getBoolean("done") || entry.getBoolean("blocked")) return true;
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)));
        if (level == null) return false;
        var id = ResourceLocation.parse("cataclysm:" + name);
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(id);
        int attempt = entry.getInt("attempt");
        int dx = attempt == 0 ? 0 : (attempt % 8) * 64 - 224;
        int dz = attempt == 0 ? 0 : (attempt / 8) * 64 - 224;
        var pos = new ChunkPos(Math.floorDiv(centerX + dx, 16), Math.floorDiv(centerZ + dz, 16));
        var generator = level.getChunkSource().getGenerator();
        boolean writing = false;
        try {
            if (structure == null) throw new IllegalStateException("Estrutura ausente: " + id);
            if (!untouched(level, pos.x, pos.z)) throw new IllegalStateException("Área já carregada ou salva; preservada");
            var start = structure.generate(level.registryAccess(), generator, generator.getBiomeSource(),
                level.getChunkSource().randomState(), server.getStructureManager(), level.getSeed(), pos, 0, level, biome -> true);
            if (!start.isValid()) throw new IllegalStateException("Candidato sem terreno válido");
            var box = start.getBoundingBox();
            // Preflight de TODA a área e de uma margem antes de carregar/escrever qualquer chunk.
            for (int x = (box.minX() >> 4) - 1; x <= (box.maxX() >> 4) + 1; x++)
                for (int z = (box.minZ() >> 4) - 1; z <= (box.maxZ() >> 4) + 1; z++)
                    if (!untouched(level, x, z)) throw new IllegalStateException("Área da dungeon já usada; preservada");
            // Reserva persistente: erro parcial jamais cria outra cópia automática.
            entry.putBoolean("blocked", true); entry.putInt("x", pos.getMinBlockX()); entry.putInt("z", pos.getMinBlockZ());
            entry.putString("dimension", dimension); data.entries.put(name, entry); data.setDirty();
            server.overworld().getDataStorage().save(); writing = true;
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++)
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) level.getChunk(x, z);
            var random = RandomSource.create(level.getSeed() ^ id.hashCode());
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
                    start.placeInChunk(level, level.structureManager(), generator, random,
                        new BoundingBox(x * 16, level.getMinBuildHeight(), z * 16, x * 16 + 15, level.getMaxBuildHeight() - 1, z * 16 + 15), new ChunkPos(x, z));
                    var target = level.getChunk(x, z);
                    target.addReferenceForStructure(structure, pos.toLong()); target.setUnsaved(true);
                }
            }
            var chunk = level.getChunk(pos.x, pos.z);
            chunk.setStartForStructure(structure, start); chunk.setUnsaved(true);
            entry.putBoolean("done", true); entry.putBoolean("blocked", false);
            data.entries.put(name, entry); data.setDirty(); server.saveEverything(false, true, true);
            LogUtils.getLogger().info("[M2 CATACLYSM] Única {} em {} {},{}", id, dimension, pos.getMinBlockX(), pos.getMinBlockZ());
            return true;
        } catch (Exception error) {
            entry.putInt("attempt", attempt + 1);
            boolean blocked = writing || attempt >= 63 || structure == null;
            entry.putBoolean("blocked", blocked); entry.putString("error", error.toString());
            data.entries.put(name, entry); data.setDirty();
            if (blocked) LogUtils.getLogger().error("[M2 CATACLYSM] {} pendente, executor suspenso para preservar o save: {}", id, error.toString());
            return blocked;
        }
    }
}
