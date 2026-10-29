package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.SprintRules;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tictim.paraglider.api.movement.Movement;
import tictim.paraglider.api.stamina.Stamina;
import tictim.paraglider.impl.movement.ServerPlayerMovement;

/**
 * Ponto de extensão EXATO: updateStamina no mod Paraglider.
 * A config runningAndSwimmingConsumesStamina continua false, evitando natação
 * alterada e gasto duplo. A drenagem acontece antes do sync do próprio mod.
 */
@Mixin(value = ServerPlayerMovement.class, remap = false)
public abstract class ParagliderSprintMixin {
    @Inject(method = "updateStamina", at = @At("TAIL"), remap = false)
    private void m2$runningConsumesSameStamina(CallbackInfo ci) {
        ServerPlayer player = ((ServerPlayerMovement)(Object)this).player();
        if (!SprintRules.running(player)) return;
        Stamina stamina = Stamina.get(player);
        if (stamina.isDepleted()) return;
        stamina.takeStamina(SprintRules.STAMINA_PER_TICK, false, false);
        Movement.get(player).setRecoveryDelay(10);
        if (stamina.stamina() <= 0 && stamina.extraStamina() <= 0) {
            stamina.setDepleted(true);
        }
    }

    @Inject(method = "update", at = @At("TAIL"), remap = false)
    private void m2$updateSpeedAfterDepletion(CallbackInfo ci) {
        SprintRules.afterParagliderUpdate(((ServerPlayerMovement)(Object)this).player());
    }
}
