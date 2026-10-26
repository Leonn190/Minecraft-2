package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PlagueEffectsMixin {
    @Inject(method="canBeAffected", at=@At("HEAD"), cancellable=true)
    private void m2$effect(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        var level = ((LivingEntity)(Object)this).level();
        if (!level.isClientSide && GameplayRules.plagueForbidden(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()), level)) cir.setReturnValue(false);
    }
}
