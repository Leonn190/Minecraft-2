package br.com.minecraft2.portal;

import com.benbenlaw.portals.integration.kubejs.PortalBuilder.PortalMaker;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import net.minecraft.resources.ResourceLocation;

/** Ponte explícita para a API Java presente no BBL Portals 1.2.2. */
public final class PortalIntegration {
    private PortalIntegration() {}

    public static void configure(PortalMaker maker, boolean pokemon) {
        ResourceLocation id = ResourceLocation.parse("m2_worldgen:" + (pokemon ? "pokemon_frame" : "arcana_frame"));
        CustomPortalApiRegistry.registerPortalFrameTester(id, () -> new DimensionPortalFrame(pokemon));
        maker.builder.customFrameTester(id)
            .registerPostTPEvent(entity -> entity.setPortalCooldown());
    }
}
