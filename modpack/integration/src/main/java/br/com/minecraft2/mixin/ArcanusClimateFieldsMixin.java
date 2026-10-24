package br.com.minecraft2.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EW reconhece quatro IDs vanilla; o fork isolado usa cópias desses parâmetros.
 * Classifica somente seus aliases Arcanus para a opção STRONG comprimir clima,
 * preservando as escalas dos ruídos de terreno Strato e todos os outros mundos.
 */
@Mixin(targets="com.exoticworlds.shape.climate.ClimateFields",remap=false)
public abstract class ArcanusClimateFieldsMixin {
    @Inject(method="isClimate",at=@At("HEAD"),cancellable=true,remap=false)
    private static void minecraft2$clonedClimate(ResourceKey<NormalNoise.NoiseParameters> key,
            CallbackInfoReturnable<Boolean> cir) {
        var id=key.location();
        if (!id.getNamespace().equals("m2arcana")) return;
        switch(id.getPath()) {
            case "vanilla/temperature", "vanilla/temperature_large", "vanilla/vegetation", "vanilla/vegetation_large" -> cir.setReturnValue(true);
            default -> { }
        }
    }
}
