package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Filtra o handler do Spore, sem cancelar a morte para os demais mods. */
@Pseudo
@Mixin(targets="com.Harbinger.Spore.Sevents.Infection", remap=false)
public abstract class SporeInfectionMixin {
    @Inject(method="onEntityDeath", at=@At("HEAD"), cancellable=true)
    private static void m2$death(LivingDeathEvent event, CallbackInfo ci) {
        if (!GameplayRules.sporeAllowed(event.getEntity().level())) ci.cancel();
    }
}
