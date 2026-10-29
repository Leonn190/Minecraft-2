package br.com.minecraft2.gameplay;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tictim.paraglider.api.stamina.Stamina;

/**
 * O Paraglider continua dono da stamina, de sua recuperação e da sincronização.
 * O multiplicador é transitório e nunca se acumula entre dimensões/respawns.
 */
public final class SprintRules {
    public static final double STAMINA_PER_TICK = 3.0;
    private static final ResourceLocation BONUS_ID = ResourceLocation.parse("m2_worldgen:stamina_sprint");
    private static final ResourceLocation PARAGLIDER_EXHAUSTION = ResourceLocation.parse("paraglider:exhaustion");
    private static final AttributeModifier BONUS = new AttributeModifier(
        BONUS_ID, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private SprintRules() {}

    public static boolean running(ServerPlayer player) {
        return player.isSprinting() && player.onGround() && !player.isSwimming()
            && !player.isInWater() && !player.isFallFlying() && !player.isPassenger()
            && !player.isCreative() && !player.isSpectator();
    }

    public static boolean hasStamina(ServerPlayer player) {
        Stamina stamina = Stamina.get(player);
        return !stamina.isDepleted() && (stamina.stamina() > 0 || stamina.extraStamina() > 0);
    }

    public static void afterParagliderUpdate(ServerPlayer player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean sprinting = running(player);
        boolean boosted = sprinting && hasStamina(player);
        if (boosted) {
            if (speed.getModifier(BONUS_ID) == null) speed.addTransientModifier(BONUS);
        } else if (speed.getModifier(BONUS_ID) != null) {
            speed.removeModifier(BONUS_ID);
        }
        // Paraglider reduz -30% da velocidade quando depleted. Durante a corrida
        // esgotada, a regra M2 pede VELOCIDADE VANILLA, não essa penalidade.
        if (sprinting && !boosted && speed.getModifier(PARAGLIDER_EXHAUSTION) != null) {
            speed.removeModifier(PARAGLIDER_EXHAUSTION);
        }
    }

    /** Somente exaustão do movimento a pé, nunca combate, cura ou mineração. */
    public static float walkingExhaustion(ServerPlayer player, float vanilla) {
        if (!running(player)) return vanilla;
        return hasStamina(player) ? 0.0f : vanilla * 2.0f;
    }
}
