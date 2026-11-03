package br.com.minecraft2.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** M2-035: durante o login, pacotes podem preceder a inicialização da câmera.
 * Usa o próprio jogador só nesse intervalo; o método original continua cuidando
 * da câmera espectadora, da dimensão e do estado toroidal, sem cancelar pacotes.
 */
@Mixin(targets = "com.exoticworlds.engine.seam.ClientPosition", remap = false)
public abstract class ExoticClientPositionMixin {
    @Redirect(method = "followCamera(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getCamera()Lnet/minecraft/world/entity/Entity;"),
            remap = false, require = 1, allow = 1)
    private Entity minecraft2$cameraDuringLogin(ServerPlayer player) {
        Entity camera = player.getCamera();
        return camera == null ? player : camera;
    }
}
