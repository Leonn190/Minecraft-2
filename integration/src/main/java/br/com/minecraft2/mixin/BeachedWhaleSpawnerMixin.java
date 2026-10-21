package br.com.minecraft2.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Este spawn ambiental usa SPAWNER no Alex's Mobs, embora não venha de um bloco spawner.
@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.world.BeachedCachalotWhaleSpawner", remap = false)
public abstract class BeachedWhaleSpawnerMixin {
    @Shadow @Final private ServerLevel world;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void m2$noEnvironmentalWhales(CallbackInfo callback) {
        String id = world.dimension().location().toString();
        if (id.equals("m2pokemon:world") || id.equals("m2arcana:world")) callback.cancel();
    }
}
