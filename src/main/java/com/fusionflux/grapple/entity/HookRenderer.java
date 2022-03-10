package com.fusionflux.grapple.entity;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.accessors.Accessors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FishingBobberEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import java.util.Objects;
import java.util.UUID;

public class HookRenderer extends EntityRenderer<HookPoint> {

    public HookRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    private static final float RADIUS = 0.02f;
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



        Vec3d end = new Vec3d(otherEntity.getLerpedPos(tickDelta).x,otherEntity.getLerpedPos(tickDelta).y+otherEntity.getHeight()+offset,otherEntity.getLerpedPos(tickDelta).z);



       if(otherEntity.getType()== EntityType.PLAYER){
           PlayerEntity playerEntity = (PlayerEntity) otherEntity;
           int j = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;
           ItemStack itemStack = playerEntity.getMainHandStack();
           if (!itemStack.isOf(Grapple.GRAPPLE)) {
               j = -j;
           }

           float h = playerEntity.getHandSwingProgress(tickDelta);
           float k = MathHelper.sin(MathHelper.sqrt(h) * (float) Math.PI);
           float l = MathHelper.lerp(tickDelta, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * (float) (Math.PI / 180.0);
           double d = MathHelper.sin(l);
           double e = MathHelper.cos(l);
           double m = (double) j * 0.35;
           double o;
           double p;
           double q;
           float r;
           if ((this.dispatcher.gameOptions == null || this.dispatcher.gameOptions.getPerspective().isFirstPerson())
                   && playerEntity == MinecraftClient.getInstance().player) {
               double s = 960.0 / this.dispatcher.gameOptions.fov;
               Vec3d vec3d = this.dispatcher.camera.getProjection().getPosition((float) j * 0.525F, -0.1F);
               vec3d = vec3d.multiply(s);
               vec3d = vec3d.rotateY(k * 0.5F);
               vec3d = vec3d.rotateX(-k * 0.7F);
               o = MathHelper.lerp(tickDelta, playerEntity.prevX, playerEntity.getX()) + vec3d.x;
               p = MathHelper.lerp(tickDelta, playerEntity.prevY, playerEntity.getY()) + vec3d.y;
               q = MathHelper.lerp(tickDelta, playerEntity.prevZ, playerEntity.getZ()) + vec3d.z;
               r = playerEntity.getStandingEyeHeight();
           } else {
               o = MathHelper.lerp(tickDelta, playerEntity.prevX, playerEntity.getX()) - e * m - d * 0.8;
               p = playerEntity.prevY + (double) playerEntity.getStandingEyeHeight() + (playerEntity.getY() - playerEntity.prevY) * (double) tickDelta - 0.45;
               q = MathHelper.lerp(tickDelta, playerEntity.prevZ, playerEntity.getZ()) - d * m + e * 0.8;
               r = playerEntity.isInSneakingPose() ? -0.1875F : 0.0F;
           }
           end = new Vec3d(o,p+r,q);
       }

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
