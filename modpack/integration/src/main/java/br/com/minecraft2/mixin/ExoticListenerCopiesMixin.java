package br.com.minecraft2.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** M2-035: sons/eventos de login também podem consultar a câmera antes do primeiro tick.
 * Preserva seleção de ouvintes, cópias toroidais e deduplicação do Exotic Worlds.
 */
@Mixin(targets = "com.exoticworlds.engine.net.ListenerCopies", remap = false)
public abstract class ExoticListenerCopiesMixin {
    @Redirect(method = "nearestTo(Lcom/exoticworlds/core/WorldFold;Ljava/lang/Iterable;Ljava/util/function/Predicate;Lnet/minecraft/world/phys/Vec3;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getCamera()Lnet/minecraft/world/entity/Entity;"),
            remap = false, require = 1, allow = 1)
    private static Entity minecraft2$listenerDuringLogin(ServerPlayer player) {
        Entity camera = player.getCamera();
        return camera == null ? player : camera;
    }
}
