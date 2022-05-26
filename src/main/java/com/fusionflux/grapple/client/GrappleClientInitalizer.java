package com.fusionflux.grapple.client;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.client.packets.GrapplePackets;
import com.fusionflux.grapple.entity.HookRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.util.TriConsumer;

public class GrappleClientInitalizer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GrapplePackets.registerPackets();
        EntityRendererRegistry.INSTANCE.register(Grapple.HOOK_POINT, HookRenderer::new);
        PredicateStuff.init(FabricModelPredicateProviderRegistry::register);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableItem) stack.getItem()).getColor(stack), Grapple.GRAPPLE);
    }
}
