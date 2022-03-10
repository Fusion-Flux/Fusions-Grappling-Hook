package com.fusionflux.grapple.entity;

import com.fusionflux.grapple.accessors.Accessors;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Objects;
import java.util.UUID;

public class HookRenderer extends EntityRenderer<HookPoint> {

    public HookRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    private static final float RADIUS = 0.055f;
    private static final Quaternion X_90_ROT = Vec3f.POSITIVE_Y.getDegreesQuaternion(90);
    private static final Vec3d UP = new Vec3d(0, 1, 0);

   @Override
   public void render(final HookPoint entity, final float yaw, final float tickDelta, final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final int light) {
       super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
       if(Objects.equals(entity.getConnected(),"null")){
           return;
       }
       final UUID uuid = UUID.fromString(entity.getConnected());
       final Entity otherEntity = ((Accessors) entity.world).getEntity(uuid);
       if (otherEntity == null) {
           return;
       }
       final Vec3d start = entity.getLerpedPos(tickDelta);
       double offset = 0;
       if(otherEntity.getType()== EntityType.PLAYER){
           offset = .1;
       }
       final Vec3d end = new Vec3d(otherEntity.getLerpedPos(tickDelta).x,otherEntity.getLerpedPos(tickDelta).y+otherEntity.getHeight()+offset,otherEntity.getLerpedPos(tickDelta).z);
       Vec3d delta = end.subtract(start);
       delta = delta.normalize();
       matrices.push();
       if (!UP.equals(delta)) {
           final Vec3d axis = UP.crossProduct(delta).normalize();
           double theta = Math.acos(UP.dotProduct(delta));
           final Vec3d b = axis.crossProduct(UP);
           if (b.dotProduct(delta) < 0) {
               theta = -theta;
           }
           float c = MathHelper.cos((float) (theta/2.0));
           float s = MathHelper.sin((float) (theta/2.0));
           final Quaternion quaternion = new Quaternion( s * (float)axis.x, s * (float)axis.y, s * (float)axis.z, c);
           matrices.multiply(quaternion);
       }
       final int endLight = dispatcher.getLight(otherEntity, tickDelta);

       render(entity,matrices, vertexConsumers, start, end, light, endLight);
       matrices.pop();
   }

    private static void render(final HookPoint entity,final MatrixStack matrixStack, final VertexConsumerProvider consumers, final Vec3d start, final Vec3d end, final int startLight, final int endLight) {
        final VertexConsumer consumer = consumers.getBuffer(RenderLayer.getEntitySolid(/*Your Texture here*/BeaconBlockEntityRenderer.BEAM_TEXTURE));
        final double length = end.distanceTo(start);
        renderCap(entity,consumer, 0, startLight, false, matrixStack);
        renderCap(entity,consumer, length, endLight, true, matrixStack);
        side(entity,matrixStack, consumer, length, startLight, endLight);
        matrixStack.multiply(X_90_ROT);
        side(entity,matrixStack, consumer, length, startLight, endLight);
        matrixStack.multiply(X_90_ROT);
        side(entity,matrixStack, consumer, length, startLight, endLight);
        matrixStack.multiply(X_90_ROT);
        side(entity,matrixStack, consumer, length, startLight, endLight);
    }

    private static void renderCap(HookPoint entity,VertexConsumer vertexConsumer, double d, int light, boolean end, MatrixStack stack) {
        int color = entity.getColor() * -1;
        if (color == -16383998) {
            color = 1908001;
        }
        if (color == 16383998) {
            color = -1908001;
        }
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;
        final Matrix4f model = stack.peek().getPositionMatrix();
        final Matrix3f normal = stack.peek().getNormalMatrix();
        if(end) {
            vertexConsumer.vertex(model, RADIUS, (float)d, -RADIUS).color(r,g,b,1f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, -RADIUS, (float)d, -RADIUS).color(r,g,b,1f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, -RADIUS, (float)d, RADIUS).color(r,g,b,1f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, RADIUS, (float)d, RADIUS).color(r,g,b,1f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
        } else {
            vertexConsumer.vertex(model, RADIUS, (float)d, RADIUS).color(r,g,b,1f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, -RADIUS, (float)d, RADIUS).color(r,g,b,1f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, -RADIUS, (float)d, -RADIUS).color(r,g,b,1f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
            vertexConsumer.vertex(model, RADIUS, (float)d, -RADIUS).color(r,g,b,1f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0, 1, 0).next();
        }
    }

    private static void side(final HookPoint entity,final MatrixStack stack, final VertexConsumer vertexConsumer, final double length, final int lightStart, final int lightEnd) {
        final Matrix4f model = stack.peek().getPositionMatrix();
        final Matrix3f normal = stack.peek().getNormalMatrix();
        //Radius of rope here
        final float rad = 0.04875f;

        int color = entity.getColor() * -1;
        if (color == -16383998) {
            color = 1908001;
        }
        if (color == 16383998) {
            color = -1908001;
        }
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0xFF00) >> 8;
        int b = color & 0xFF;


        vertexConsumer.vertex(model, RADIUS, 0, -RADIUS).color(r,g,b,1f).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(lightStart).normal(normal, 1, 0, 0).next();
        vertexConsumer.vertex(model, RADIUS, (float) length, -RADIUS).color(r,g,b,1f).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(lightEnd).normal(normal, 1, 0, 0).next();
        vertexConsumer.vertex(model, RADIUS, (float) length, RADIUS).color(r,g,b,1f).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(lightEnd).normal(normal, 1, 0, 0).next();
        vertexConsumer.vertex(model, RADIUS, 0, RADIUS).color(r,g,b,1f).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(lightStart).normal(normal, 1, 0, 0).next();
    }



    @Override
    public Identifier getTexture(HookPoint entity) {
        return new Identifier("entitytesting", "textures/entity/cube/cube.png");
    }
}
