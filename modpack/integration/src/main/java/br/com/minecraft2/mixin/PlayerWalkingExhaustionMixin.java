package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.SprintRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Intercepta exclusivamente a exaustão por deslocamento, não addExhaustion global. */
@Mixin(Player.class)
public abstract class PlayerWalkingExhaustionMixin {
    @ModifyArg(method = "checkMovementStatistics",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
        index = 0, require = 1)
    private float m2$sprintOnlyFood(float vanilla) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            return SprintRules.walkingExhaustion(serverPlayer, vanilla);
        }
        return vanilla;
    }
}
