package com.fusionflux.grapple.client;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.entity.HookModel;
import com.fusionflux.grapple.entity.HookRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class GrappleClientInitalizer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(HookModel.MAIN_LAYER, HookModel::getTexturedModelData);
        EntityRendererRegistry.INSTANCE.register(Grapple.HOOK_POINT, HookRenderer::new);

    }
}
