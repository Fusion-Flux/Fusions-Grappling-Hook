package com.fusionflux.grapple.entity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class HookRenderer extends EntityRenderer<HookPoint> {

    public HookRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(HookPoint entity) {
        return new Identifier("entitytesting", "textures/entity/cube/cube.png");
    }
}
