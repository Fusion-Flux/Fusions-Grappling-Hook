package com.fusionflux.grapple.client;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.client.packets.GrapplePackets;
import com.fusionflux.grapple.entity.HookRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

public class GrappleClientInitalizer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        GrapplePackets.registerPackets();
        EntityRendererRegistry.INSTANCE.register(Grapple.HOOK_POINT, HookRenderer::new);
    }
}
