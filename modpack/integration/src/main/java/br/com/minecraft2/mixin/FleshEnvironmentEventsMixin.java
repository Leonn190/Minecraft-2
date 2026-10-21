package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.FEvents.HandleEvents", remap=false)
public abstract class FleshEnvironmentEventsMixin {
    @Inject(method="onLivingSpawned", at=@At("HEAD"), cancellable=true)
    private static void m2$onLivingSpawned(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getLevel())) ci.cancel();
    }
    @Inject(method="onLivingDeath", at=@At("HEAD"), cancellable=true)
    private static void m2$onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getEntity().level())) ci.cancel();
    }
    @Inject(method="onBlockBreak", at=@At("HEAD"), cancellable=true)
    private static void m2$onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getPlayer().level())) ci.cancel();
    }

}
