package br.com.minecraft2.gameplay;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.crafting.BackpackUpgradeRecipe;
import net.p3pp3rf1y.sophisticatedcore.crafting.RecipeWrapperSerializer;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointStackState;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackLifecycle;

/** Upgrade nativo com capacidade monotônica: a ordem visual M2 inclui tiers menores. */
public final class BackpackProgressionRecipe extends BackpackUpgradeRecipe {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, "m2_worldgen");
    private static final java.util.function.Supplier<RecipeSerializer<BackpackProgressionRecipe>> SERIALIZER =
        SERIALIZERS.register("backpack_progression", () ->
            new RecipeWrapperSerializer<>(BackpackProgressionRecipe::new, RecipeSerializer.SHAPED_RECIPE));

    public static void register(IEventBus bus) { SERIALIZERS.register(bus); }

    public BackpackProgressionRecipe(ShapedRecipe recipe) { super(recipe); }

    @Override
    public RecipeSerializer<?> getSerializer() { return SERIALIZER.get(); }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = getCompose().assemble(input, registries);
        for (int i = 0; i < input.size(); i++) {
            ItemStack source = input.getItem(i);
            if (!(source.getItem() instanceof BackpackItem previous)) continue;
            BackpackItem next = (BackpackItem) result.getItem();
            // Mesmo contrato nativo: UUID, inventário, cores, nome, filtros e upgrades.
            result.applyComponents(source.getComponents());
            int slots = Math.max(next.getNumberOfSlots(), source.getOrDefault(
                ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, previous.getNumberOfSlots()));
            int upgrades = Math.max(next.getNumberOfUpgradeSlots(), source.getOrDefault(
                ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, previous.getNumberOfUpgradeSlots()));
            // Endpoint vinculado não deve abrir ou modificar o inventário compartilhado.
            if (LinkedStorageStackLifecycle.classifyEndpoint(result) == LinkedStorageEndpointStackState.ENDPOINT) {
                result.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, slots);
                result.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, upgrades);
            } else {
                BackpackWrapper.fromStack(result).setSlotNumbers(slots, upgrades);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }
}
