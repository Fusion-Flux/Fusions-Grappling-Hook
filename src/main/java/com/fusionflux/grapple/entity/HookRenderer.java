package com.fusionflux.grapple.entity;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.accessors.Accessors;
import com.fusionflux.grapple.items.GrappleItem;
import me.andrew.gravitychanger.api.GravityChangerAPI;
import me.andrew.gravitychanger.util.RotationUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
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

    public static final Identifier ROPE_TEXTURE = new Identifier("grapple:textures/entity/rope.png");

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



        Vec3d lineStart = new Vec3d(otherEntity.getLerpedPos(tickDelta).x,otherEntity.getLerpedPos(tickDelta).y+otherEntity.getHeight()+offset,otherEntity.getLerpedPos(tickDelta).z);



       if(otherEntity.getType()== EntityType.PLAYER){
           Direction gravityDirection = (GravityChangerAPI.getGravityDirection(otherEntity));
           PlayerEntity playerEntity = (PlayerEntity) otherEntity;
           int armOffset = playerEntity.getMainArm() == Arm.RIGHT ? 1 : -1;
           ItemStack itemStack = playerEntity.getMainHandStack();
           if (!itemStack.isOf(Grapple.GRAPPLE)) {
               armOffset = -armOffset;
           }

           float handSwingProgress = playerEntity.getHandSwingProgress(tickDelta);
           float sinHandSwingProgress = MathHelper.sin(MathHelper.sqrt(handSwingProgress) * 3.1415927F);
           float radBodyYaw = MathHelper.lerp(tickDelta, playerEntity.prevBodyYaw, playerEntity.bodyYaw) * 0.017453292F;
           double sinBodyYaw = MathHelper.sin(radBodyYaw);
           double cosBodyYaw = MathHelper.cos(radBodyYaw);
           double scaledArmOffset = (double) armOffset * 0.35D;
           //Vec3d lineStart;
           if ((this.dispatcher.gameOptions == null || this.dispatcher.gameOptions.getPerspective().isFirstPerson()) && playerEntity == MinecraftClient.getInstance().player) {
               Vec3d lineOffset = RotationUtil.vecWorldToPlayer(this.dispatcher.camera.getProjection().getPosition((float) armOffset * 0.4F, -0.1F), gravityDirection);
               lineOffset = lineOffset.multiply(860.0D / this.dispatcher.gameOptions.fov);
               lineOffset = lineOffset.rotateY(sinHandSwingProgress * 0.5F);
               lineOffset = lineOffset.rotateX(-sinHandSwingProgress * 0.7F);
               lineStart = new Vec3d(
                       MathHelper.lerp(tickDelta, playerEntity.prevX, playerEntity.getX()),
                       MathHelper.lerp(tickDelta, playerEntity.prevY, playerEntity.getY()),
                       MathHelper.lerp(tickDelta, playerEntity.prevZ, playerEntity.getZ())
               ).add(RotationUtil.vecPlayerToWorld(lineOffset.add(0.0D, playerEntity.getStandingEyeHeight()-.2, 0.0D), gravityDirection));
           } else {
               lineStart = new Vec3d(
                       MathHelper.lerp(tickDelta, playerEntity.prevX, playerEntity.getX()),
                       playerEntity.prevY + (playerEntity.getY() - playerEntity.prevY) * tickDelta,
                       MathHelper.lerp(tickDelta, playerEntity.prevZ, playerEntity.getZ())
               ).add(RotationUtil.vecPlayerToWorld(
                       -cosBodyYaw * scaledArmOffset - sinBodyYaw * 0.7D,
                       playerEntity.getStandingEyeHeight()-.75 + (playerEntity.isInSneakingPose() ? -0.1875D : 0.0D) - 0.45D,
                       -sinBodyYaw * scaledArmOffset + cosBodyYaw * 0.7D,
                       gravityDirection
               ));
           }
       }

       Vec3d delta = lineStart.subtract(start);
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

       render(entity,matrices, vertexConsumers, start, lineStart, light, endLight);
       matrices.pop();
   }

    private static void render(final HookPoint entity,final MatrixStack matrixStack, final VertexConsumerProvider consumers, final Vec3d start, final Vec3d end, final int startLight, final int endLight) {
        final VertexConsumer consumer = consumers.getBuffer(RenderLayer.getEntitySolid(/*Your Texture here*/ROPE_TEXTURE));
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
        final Matrix4f model = stack.peek().getModel();
        final Matrix3f normal = stack.peek().getNormal();
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
        final Matrix4f model = stack.peek().getModel();
        final Matrix3f normal = stack.peek().getNormal();
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
