package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.FEvents.FleshWorld", remap=false)
public abstract class FleshProgressionEventsMixin {
    @Inject(method="onEntityKilled", at=@At("HEAD"), cancellable=true)
    private static void m2$onEntityKilled(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getEntity().level())) ci.cancel();
    }

}
