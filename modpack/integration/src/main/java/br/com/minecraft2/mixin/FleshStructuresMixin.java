package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.GameplayRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.features.structures.FStructures", remap=false)
public abstract class FleshStructuresMixin {
    @Inject(method="spawnStructure", at=@At("HEAD"), cancellable=true)
    private void m2$structure(ServerLevel level, BlockPos pos, String name, CallbackInfo ci) {
        if (!GameplayRules.fleshAllowed(level)) ci.cancel();
    }
}
