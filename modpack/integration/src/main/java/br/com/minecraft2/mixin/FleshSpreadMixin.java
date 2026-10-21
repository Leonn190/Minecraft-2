package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.FleshBlockSpread", remap=false)
public abstract class FleshSpreadMixin {
    @Shadow protected ServerLevel level;
    @Unique private String m2$dimension;

    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    private void m2$spread(CallbackInfo ci) {
        if (level == null || !GameplayRules.fleshAllowed(level)) ci.cancel();
    }

    // Upstream salva todas as filas no Overworld e perde a dimensão ao reiniciar.
    // Acrescenta apenas um campo próprio; NBT antigo continua associado ao Overworld.
    @Inject(method="saveStateToNBT", at=@At("RETURN"))
    private void m2$save(CallbackInfoReturnable<CompoundTag> cir) {
        if (level != null || m2$dimension != null) cir.getReturnValue().putString("m2_dimension", level != null ? level.dimension().location().toString() : m2$dimension);
    }
    @Inject(method="loadStateFromNBT", at=@At("TAIL"))
    private void m2$load(CompoundTag tag, CallbackInfo ci) {
        m2$dimension = tag.contains("m2_dimension") ? tag.getString("m2_dimension") : "minecraft:overworld";
    }
    @Inject(method="setLevel", at=@At("HEAD"), cancellable=true)
    private void m2$restore(ServerLevel supplied, CallbackInfo ci) {
        if (m2$dimension != null) {
            ResourceLocation id = ResourceLocation.tryParse(m2$dimension);
            level = id == null ? null : supplied.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
            ci.cancel();
        }
    }
}
