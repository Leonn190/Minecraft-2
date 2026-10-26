package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Incubadoras e outros block entities antigos permanecem salvos, mas inertes. */
@Mixin(targets="net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public abstract class PlagueBlockEntityMixin {
    @Shadow @Final private BlockEntity blockEntity;
    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    private void m2$tick(CallbackInfo ci) {
        var level = blockEntity.getLevel();
        if (level != null && !level.isClientSide && GameplayRules.plagueForbidden(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()), level)) ci.cancel();
    }
}
