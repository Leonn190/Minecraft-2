package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.OneOfUsEffect", remap=false)
public abstract class FleshInfectionAttackMixin {
    @Inject(method="doHurtTarget", at=@At("HEAD"), cancellable=true)
    private static void m2$doHurtTarget(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getEntity().level())) ci.cancel();
    }

}
