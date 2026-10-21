package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.potion.OneOfUsMobEffect", remap=false)
public abstract class FleshTransformationMixin {
    @Inject(method="onLivingDeath", at=@At("HEAD"), cancellable=true)
    private void m2$onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(event.getEntity().level())) ci.cancel();
    }

}
