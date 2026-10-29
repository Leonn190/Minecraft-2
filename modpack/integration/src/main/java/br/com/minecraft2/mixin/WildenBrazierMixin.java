package br.com.minecraft2.mixin;

import br.com.minecraft2.gameplay.ChimeraRules;
import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import com.hollingsworth.arsnouveau.common.ritual.RitualWildenSummoning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Os itens de augmento são consumidos por tryBurnStack, antes de canStart!
 * A recusa deve ocorrer ANTES de stack.split(1).
 */
@Mixin(value = RitualBrazierTile.class, remap = false)
public abstract class WildenBrazierMixin {
    @Shadow public AbstractRitual ritual;

    @Inject(method = "tryBurnStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void m2$rejectOutsideArcanus(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        RitualBrazierTile tile = (RitualBrazierTile)(Object)this;
        if (tile.getLevel() instanceof ServerLevel level
                && ritual instanceof RitualWildenSummoning
                && ritual.canConsumeItem(stack)
                && !ChimeraRules.allowed(level.dimension().location())) {
            ChimeraRules.warnNearby(level, tile.getBlockPos());
            cir.setReturnValue(false); // a pilha permanece intacta
        }
    }

    @Inject(method = "startRitual(Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void m2$rejectPreparedBoss(@Nullable Player player, CallbackInfo ci) {
        RitualBrazierTile tile = (RitualBrazierTile)(Object)this;
        if (tile.getLevel() instanceof ServerLevel level
                && ritual instanceof RitualWildenSummoning wilden && wilden.isBossSpawn()
                && !ChimeraRules.allowed(level.dimension().location())) {
            if (player != null) ChimeraRules.warn(player);
            else ChimeraRules.warnNearby(level, tile.getBlockPos());
            ci.cancel(); // protege também braseiros antigos com augmentos já armazenados
        }
    }
}
