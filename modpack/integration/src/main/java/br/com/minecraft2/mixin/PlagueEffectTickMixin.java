package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Efeito trazido por portal/salvo fora do habitat expira antes de executar. */
@Mixin(MobEffectInstance.class)
public abstract class PlagueEffectTickMixin {
    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    private void m2$tick(LivingEntity entity, Runnable updated, CallbackInfoReturnable<Boolean> cir) {
        var effect = (MobEffectInstance)(Object)this;
        if (!entity.level().isClientSide && GameplayRules.plagueForbidden(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()), entity.level())) cir.setReturnValue(false);
    }
}
