package br.com.minecraft2;

import com.exoticworlds.api.v1.ExoticWorldsApi;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Diagnóstico sem conversão de saves, substituição de geradores ou teleporte. */
public final class WorldShapeDiagnostics {
    public static final Map<String,Integer> WIDTHS = Map.of(
        "minecraft:overworld",9984,"minecraft:the_nether",4992,"minecraft:the_end",6016,
        "m2pokemon:world",4992,"m2arcana:world",3008,
        "undergarden:undergarden",4992,"macabre:the_pit",2016);

    public static void started(ServerStartedEvent event) {
        for (var level : event.getServer().getAllLevels()) {
            String id = level.dimension().location().toString();
            if (!WIDTHS.containsKey(id)) continue;
            var shape = ExoticWorldsApi.shapeOf(level);
            var generator = level.getChunkSource().getGenerator();
            if (shape.isEmpty() || shape.get().widthBlocks(Direction.Axis.X) != WIDTHS.get(id)
                    || shape.get().widthBlocks(Direction.Axis.Z) != WIDTHS.get(id)) {
                LogUtils.getLogger().error("[M2 SHAPE] {}: limites ausentes/divergentes; esperado {} blocos X/Z; gerador {}. Em mundo NOVO selecione m2worlds:toroidal. Save existente não foi convertido.", id, WIDTHS.get(id), generator.getClass().getName());
            } else {
                LogUtils.getLogger().info("[M2 SHAPE] {}: {} blocos X/Z; gerador {}; identidade/persistência exigem auditoria de servidor.", id, WIDTHS.get(id), generator.getClass().getName());
            }
        }
    }

    private WorldShapeDiagnostics() {}
}
