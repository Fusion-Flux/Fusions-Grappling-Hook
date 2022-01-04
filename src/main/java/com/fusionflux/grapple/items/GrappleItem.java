package com.fusionflux.grapple.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GrappleItem extends Item {

    public GrappleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        NbtCompound tag = stack.getOrCreateNbt();
        HitResult hitResult = user.raycast(128.0D, 0.0F, false);
        Vec3d hitPos;
        double distance;
        boolean isHooked = tag.getBoolean("isHooked");


        if(!isHooked) {
            //Check to make sure the HitResult hits a block
            if (hitResult.getType() == HitResult.Type.BLOCK) {

                //Gets the position of the HitResult
                hitPos = ((BlockHitResult) hitResult).getPos();

                stack.getOrCreateNbt().putDouble("hitPosX", hitPos.x);
                stack.getOrCreateNbt().putDouble("hitPosY", hitPos.y);
                stack.getOrCreateNbt().putDouble("hitPosZ", hitPos.z);

                //Gets the distance between the HitResult Position and the Players Position
                distance = hitPos.distanceTo(user.getEyePos());

                stack.getOrCreateNbt().putDouble("distance", distance);

                stack.getOrCreateNbt().putBoolean("isHooked", true);
            }
        }
        if(isHooked){
            stack.getOrCreateNbt().putBoolean("isHooked", false);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        NbtCompound tag = stack.getOrCreateNbt();

        double distance = tag.getDouble("distance");
        Vec3d hitPos = new Vec3d(tag.getDouble("hitPosX"),tag.getDouble("hitPosY"),tag.getDouble("hitPosZ"));
        Vec3d grappleVector = hitPos.subtract(entity.getPos());
        Vec3d planeBasis = new Vec3d( grappleVector.getX(), 0, grappleVector.getZ() );
        double cosOfElevation = grappleVector.dotProduct( planeBasis )
                / ( Math.abs( grappleVector.length()) * Math.abs( planeBasis.length() ));
        // radians
        double angleOfElevation = Math.acos( cosOfElevation );
        // radians to degrees
        angleOfElevation = Math.toDegrees( angleOfElevation );

        boolean isHooked = tag.getBoolean("isHooked");
        if(isHooked){
            Vec3d gravity = new Vec3d( 0.00, -0.08, 0.00 );
            Vec3d veloc = entity.getVelocity();
            double velocLength = veloc.length();
            Vec3d grappleDirection = grappleVector.normalize();
            Vec3d radialComponent = grappleDirection.negate().multiply( veloc.dotProduct( grappleDirection ));
            Vec3d tangentialComponent = gravity.subtract( radialComponent );
            double tangentialLength = tangentialComponent.length();

            Vec3d tangentialAccel = tangentialComponent.normalize().multiply( tangentialLength * tangentialLength / grappleVector.length() + gravity.length() ).negate();
            entity.setVelocity( entity.getVelocity().add( tangentialAccel ));

        }

    }

}
