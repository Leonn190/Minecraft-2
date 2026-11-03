package br.com.minecraft2.gameplay;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Uma aplicação por entrada; estado só em memória no servidor, nunca no save. */
public final class UndergardenWater {
    private static final Set<UUID> IN_WATER = new HashSet<>();
    private UndergardenWater() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(UndergardenWater::tick);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::login);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::logout);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::respawn);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::dimension);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::clonePlayer);
        NeoForge.EVENT_BUS.addListener(UndergardenWater::stop);
    }

    private static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        // isInWater usa contato corporal com água corrente/parada; não inclui chuva.
        boolean contact = player.isAlive()
            && player.level().dimension().location().toString().equals("undergarden:undergarden")
            && player.isInWater();
        if (!contact) { IN_WATER.remove(id); return; }
        if (IN_WATER.add(id)) player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
    }

    private static void login(PlayerEvent.PlayerLoggedInEvent e) { IN_WATER.remove(e.getEntity().getUUID()); }
    private static void logout(PlayerEvent.PlayerLoggedOutEvent e) { IN_WATER.remove(e.getEntity().getUUID()); }
    private static void respawn(PlayerEvent.PlayerRespawnEvent e) { IN_WATER.remove(e.getEntity().getUUID()); }
    private static void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { IN_WATER.remove(e.getEntity().getUUID()); }
    private static void clonePlayer(PlayerEvent.Clone e) {
        IN_WATER.remove(e.getOriginal().getUUID());
        IN_WATER.remove(e.getEntity().getUUID());
    }
    private static void stop(ServerStoppedEvent e) { IN_WATER.clear(); }
}
