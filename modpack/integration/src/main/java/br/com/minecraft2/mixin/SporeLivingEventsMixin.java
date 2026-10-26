package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="com.Harbinger.Spore.Sevents.LivingTickEvent", remap=false)
public abstract class SporeLivingEventsMixin {
    @Inject(method="TickEvents", at=@At("HEAD"), cancellable=true)
    private static void m2$living(EntityTickEvent.Pre event, CallbackInfo ci) {
        if (!GameplayRules.sporeAllowed(event.getEntity().level())) ci.cancel();
    }
    @Inject(method="TickEffects", at=@At("HEAD"), cancellable=true)
    private static void m2$player(PlayerTickEvent.Pre event, CallbackInfo ci) {
        if (!GameplayRules.sporeAllowed(event.getEntity().level())) ci.cancel();
    }
}
