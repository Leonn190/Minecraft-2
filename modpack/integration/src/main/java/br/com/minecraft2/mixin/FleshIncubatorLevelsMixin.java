package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
@Pseudo
@Mixin(targets={"net.mcreator.thefleshthathates.IncubatorSpawn", "net.mcreator.thefleshthathates.IncubatorMobSpawn", "net.mcreator.thefleshthathates.FEvents.DeleteFleshMob"}, remap=false)
public abstract class FleshIncubatorLevelsMixin {
    // Filtra a enumeração original; preserva intervalos, tentativas e condições do mod.
    @Redirect(method="onServerTick", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> m2$levels(MinecraftServer server) {
        List<ServerLevel> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) if (GameplayRules.fleshAllowed(level)) result.add(level);
        return result;
    }
}
