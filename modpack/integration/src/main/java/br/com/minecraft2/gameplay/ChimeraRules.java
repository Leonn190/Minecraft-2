package br.com.minecraft2.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** O bloqueio de criação NÃO afeta outras criaturas ou rituais Ars. */
public final class ChimeraRules {
    private static final ResourceLocation CHIMERA = ResourceLocation.parse("ars_nouveau:wilden_chimera");
    private static final ResourceLocation ARCANUS = ResourceLocation.parse("m2arcana:world");
    private static final Component MESSAGE = Component.literal("A Wilden Chimera só pode ser invocada em Arcanus.");
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, Long> LAST_NOTICE = new java.util.concurrent.ConcurrentHashMap<>();

    private ChimeraRules() {}

    public static boolean allowed(ResourceLocation dimension) {
        return ARCANUS.equals(dimension);
    }

    public static void warn(Player player) {
        player.displayClientMessage(MESSAGE, true);
    }

    public static void warnNearby(ServerLevel level, net.minecraft.core.BlockPos pos) {
        // A primeira tentativa avisa imediatamente; cooldown evita spam por itens automáticos.
        long now = level.getGameTime();
        for (Player player : level.players()) {
            if (player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64) continue;
            Long last = LAST_NOTICE.get(player.getUUID());
            if (last == null || now < last || now - last >= 40) {
                LAST_NOTICE.put(player.getUUID(), now);
                warn(player);
            }
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, ChimeraRules::join);
    }

    private static void join(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) return;
        ResourceLocation type = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (CHIMERA.equals(type) && !allowed(event.getLevel().dimension().location())) event.setCanceled(true);
    }
}
