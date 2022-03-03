package com.fusionflux.grapple.items;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


        if (!isHooked) {
            //Check to make sure the HitResult hits a block
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                //world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1F, 1F);

                //Gets the position of the HitResult
                hitPos = hitResult.getPos();

                BlockSoundGroup blockSoundGroup = world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getSoundGroup();

                world.playSound(null, hitPos.x,hitPos.y,hitPos.z, blockSoundGroup.getBreakSound(), SoundCategory.NEUTRAL, 1F, blockSoundGroup.getPitch());
                world.syncWorldEvent(user, 2001, ((BlockHitResult) hitResult).getBlockPos(), Block.getRawIdFromState(world.getBlockState(((BlockHitResult) hitResult).getBlockPos())));

                System.out.println(hitPos.x);
                stack.getOrCreateNbt().putDouble("hitPosX", hitPos.x);
                stack.getOrCreateNbt().putDouble("hitPosY", hitPos.y);
                stack.getOrCreateNbt().putDouble("hitPosZ", hitPos.z);

                List<Long> posXList = new ArrayList<>();
                List<Long> posYList = new ArrayList<>();
                List<Long> posZList = new ArrayList<>();
                
                
                posXList.add(Double.doubleToLongBits(hitPos.x));
                posYList.add(Double.doubleToLongBits(hitPos.y));
                posZList.add(Double.doubleToLongBits(hitPos.z));

                stack.getOrCreateNbt().putLongArray("xList", posXList);
                stack.getOrCreateNbt().putLongArray("yList", posYList);
                stack.getOrCreateNbt().putLongArray("zList", posZList);
                
                //Gets the distance between the HitResult Position and the Players Position
                distance = hitPos.distanceTo(user.getEyePos());
                

                stack.getOrCreateNbt().putDouble("distance", distance);

                stack.getOrCreateNbt().putBoolean("isHooked", true);
            }
        }
        if (isHooked) {
            stack.getOrCreateNbt().putBoolean("isHooked", false);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        NbtCompound tag = stack.getOrCreateNbt();

       /* int ticks = tag.getInt("noMoveTime");

        if (entity.getVelocity().x == 0 && entity.getVelocity().z == 0 && entity.isOnGround()) {
            ticks++;
            stack.getOrCreateNbt().putInt("noMoveTime", ticks);
        } else {
            stack.getOrCreateNbt().putInt("noMoveTime", 0);
        }

        if (ticks >= 120) {
            entity.setOnFire(true);
        }*/

        long[] xList = tag.getLongArray("xList");


        List<Long> posXList = Arrays.asList(ArrayUtils.toObject(test));
        List<Long> posYList = new ArrayList<>();
        List<Long> posZList = new ArrayList<>();




        double distance = tag.getDouble("distance");
        Vec3d hitPos = new Vec3d(tag.getDouble("hitPosX"), tag.getDouble("hitPosY"), tag.getDouble("hitPosZ"));
        boolean isHooked = tag.getBoolean("isHooked");
        if (isHooked && !entity.isOnGround()) {
            ((PlayerEntity) entity).setNoDrag(true);
            entity.setNoGravity(true);

            Vec3d grappleVector = hitPos.subtract(entity.getEyePos());
            Vec3d horizontalCorrection  = grappleVector.normalize().multiply((.08) + (Math.abs(entity.getVelocity().y) * .08), .08, (.08) + (Math.abs(entity.getVelocity().y) * .08));
            double testy = horizontalCorrection.y;
            horizontalCorrection = horizontalCorrection.multiply(1, 0, 1);

            if (hitPos.y > entity.getEyePos().y) {

                if (hitPos.distanceTo(entity.getEyePos()) > distance) {
                    entity.setVelocity(entity.getVelocity().add(horizontalCorrection));
                }
                if (hitPos.distanceTo(entity.getEyePos()) > distance) {
                    entity.setVelocity(entity.getVelocity().add(new Vec3d(0, .08, 0)));
                }
                if (hitPos.distanceTo(entity.getEyePos()) > distance+.1) {

                    if (entity.getVelocity().y < -.08) {
                         entity.setVelocity(entity.getVelocity().add(new Vec3d(0, testy, 0)));
                        // entity.setVelocity(new Vec3d(entity.getVelocity().x, -testy, entity.getVelocity().z));
                    }
                }


                if (hitPos.distanceTo(entity.getEyePos()) < distance) {
                    entity.setVelocity(entity.getVelocity().add(new Vec3d(0, -.08, 0)));
                }

            }

            if (hitPos.y < entity.getEyePos().y) {
                if (hitPos.distanceTo(entity.getEyePos()) > distance + 1) {

                    if (entity.getVelocity().y < 0) {
                        entity.setVelocity(entity.getVelocity().add(new Vec3d(0, -entity.getVelocity().y / 4, 0)));
                    }

                }
                if (hitPos.distanceTo(entity.getEyePos()) > distance) {
                    entity.setVelocity(entity.getVelocity().add(horizontalCorrection));
                }

                entity.setVelocity(entity.getVelocity().add(new Vec3d(0, -.08, 0)));
            }


            entity.setVelocity(entity.getVelocity().multiply(.99));


        } else {
            ((PlayerEntity) entity).setNoDrag(false);
            entity.setNoGravity(false);
        }

        if (isHooked && entity.isOnGround()) {
            distance = hitPos.distanceTo(entity.getEyePos());

            stack.getOrCreateNbt().putDouble("distance", distance);
        }

    }

}
