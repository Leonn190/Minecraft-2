package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.entity.LivingEntity;
@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.potion.OneOfUsMobEffect", remap=false)
public abstract class FleshInfectionEffectMixin {
    @Inject(method="applyEffectTick", at=@At("HEAD"), cancellable=true)
    private void m2$effect(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (!GameplayRules.fleshAllowed(entity.level())) cir.setReturnValue(true);
    }
}
