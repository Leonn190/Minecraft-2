package br.com.minecraft2.gameplay;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Regras de servidor: sem editar atributos, inventários ou registros de terceiros. */
public final class GameplayRules {
    private GameplayRules() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, GameplayRules::damage);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, GameplayRules::join);
        NeoForge.EVENT_BUS.addListener(GameplayRules::entityTick);
    }

    public static boolean fleshAllowed(Level level) {
        return fleshAllowed(level.dimension().location().toString());
    }

    public static boolean fleshAllowed(String dimension) {
        return dimension.equals("m2pokemon:world") || dimension.equals("m2arcana:world");
    }

    public static boolean flesh(ResourceLocation id) {
        return id.getNamespace().equals("the_flesh_that_hates");
    }

    /** Projéteis devem expor a arma original; nunca consultar a mão atual do atirador. */
    public static ItemStack weapon(DamageSource source) {
        if (source.getDirectEntity() == null) return ItemStack.EMPTY;
        if (source.getDirectEntity() instanceof LivingEntity) {
            // Espinhos, queimaduras e habilidades de contato não são golpes com a arma.
            if (!source.is(DamageTypes.PLAYER_ATTACK) && !source.is(DamageTypes.MOB_ATTACK)
                    && !source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) return ItemStack.EMPTY;
        }
        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) return ItemStack.EMPTY;
        if (source.getDirectEntity() instanceof LivingEntity) {
            boolean[] armed = {false};
            weapon.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                if (attribute.equals(Attributes.ATTACK_DAMAGE) && modifier.amount() > 0) armed[0] = true;
            });
            if (!armed[0]) return ItemStack.EMPTY;
        }
        return weapon;
    }

    public static float adjusted(float amount, String dimension, String weaponNamespace) {
        return dimension.equals("undergarden:undergarden") && weaponNamespace != null
            && !weaponNamespace.equals("undergarden") ? amount * 0.40F : amount;
    }

    private static void damage(LivingIncomingDamageEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide || !level.dimension().location().toString().equals("undergarden:undergarden")) return;
        ItemStack weapon = weapon(event.getSource());
        if (!weapon.isEmpty()) event.setAmount(adjusted(event.getAmount(), level.dimension().location().toString(),
            BuiltInRegistries.ITEM.getKey(weapon.getItem()).getNamespace()));
    }

    private static void join(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && !fleshAllowed(event.getLevel()) && event.getEntity() instanceof Mob
                && flesh(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()))
                && !event.loadedFromDisk()) event.setCanceled(true);
    }

    private static void entityTick(EntityTickEvent.Pre event) {
        // Mobs de saves existentes ficam inertes, sem serem apagados ou varridos por tick.
        if (event.getEntity() instanceof Mob && !event.getEntity().level().isClientSide
                && flesh(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()))
                && !fleshAllowed(event.getEntity().level())) event.setCanceled(true);
    }
}
