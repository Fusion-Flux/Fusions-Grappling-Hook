package com.fusionflux.grapple.items;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.entity.HookPoint;
import com.fusionflux.gravity_api.api.GravityChangerAPI;
import com.fusionflux.gravity_api.util.RotationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class GrappleItem extends Item implements DyeableItem {

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
                world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.NEUTRAL, 1F, 1F);
                world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.NEUTRAL, 1F, 1F);
                world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.NEUTRAL, 1F, 1F);
                world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.NEUTRAL, 1F, 1F);
                world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, SoundCategory.NEUTRAL, 1F, 1F);

                //Gets the position of the HitResult
                hitPos = hitResult.getPos();

                BlockSoundGroup blockSoundGroup = world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getSoundGroup();

                world.playSound(null, hitPos.x,hitPos.y,hitPos.z, blockSoundGroup.getBreakSound(), SoundCategory.NEUTRAL, 1F, blockSoundGroup.getPitch());
                world.syncWorldEvent(user, 2001, ((BlockHitResult) hitResult).getBlockPos(), Block.getRawIdFromState(world.getBlockState(((BlockHitResult) hitResult).getBlockPos())));


                List<Long> posXList = new ArrayList<>();
                List<Long> posYList = new ArrayList<>();
                List<Long> posZList = new ArrayList<>();


                HookPoint hookPoint;
                hookPoint = Grapple.HOOK_POINT.create(world);
                assert hookPoint != null;
                hookPoint.setPos(hitPos.x,hitPos.y,hitPos.z);
                hookPoint.setConnected(user.getUuidAsString());
                hookPoint.setColor(this.getColor(stack));
                if (!world.isClient) {
                    world.spawnEntity(hookPoint);
                }
                NbtList test = stack.getOrCreateNbt().getList("entities",11);
                test.add(NbtHelper.fromUuid(hookPoint.getUuid()));
                stack.getOrCreateNbt().put("entities", test);

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
                stack.getOrCreateNbt().putBoolean("isHookInUse", true);
                stack.getOrCreateNbt().putBoolean("initalBoost", false);
                stack.getOrCreateNbt().putBoolean("grappleToggle", true);

            }
        }
        if (isHooked) {
            world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, SoundCategory.NEUTRAL, 1F, 2F);
            world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.NEUTRAL, 1F, 2F);
            world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, SoundCategory.NEUTRAL, 1F, 2F);
            world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, SoundCategory.NEUTRAL, 1F, 2F);
            world.playSound(null, user.getPos().x,user.getPos().y,user.getPos().z, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, SoundCategory.NEUTRAL, 1F, 2F);

            NbtList UUIDs = stack.getOrCreateNbt().getList("entities",11);

            for (NbtElement uuid : UUIDs) {
                if (!world.isClient) {
                    HookPoint hookPoint = (HookPoint) ((ServerWorld) world).getEntity(NbtHelper.toUuid(uuid));
                    if(hookPoint != null)
                    hookPoint.kill();
                }
            }

            NbtList clearUUIDs = new NbtList();
            stack.getOrCreateNbt().put("entities", clearUUIDs);


            stack.getOrCreateNbt().putBoolean("isHooked", false);
        }

        return TypedActionResult.pass(stack);
    }

    public static int getIsHooked(ItemStack stack) {
        if(stack.getOrCreateNbt().getBoolean("isHooked")){
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        boolean holdingHook = false;
        for (ItemStack test : entity.getItemsHand()) {
            if (test == stack) {
                holdingHook = true;
                break;
            }
        }
        Vec3d adjustmentPos = RotationUtil.vecPlayerToWorld(0d,(entity.getHeight()/2),0d, GravityChangerAPI.getGravityDirection(entity));
        Vec3d entityPos = new Vec3d(entity.getPos().x+ adjustmentPos.x,entity.getPos().y + adjustmentPos.y,entity.getPos().z+ adjustmentPos.z);
            NbtCompound tag = stack.getOrCreateNbt();
        boolean isHookInUse = tag.getBoolean("isHookInUse");
        boolean grappleToggle = tag.getBoolean("grappleToggle");
            if (isHookInUse)
                if (holdingHook) {
                    entity.fallDistance = 0;
                    boolean initalBoost = tag.getBoolean("initalBoost");
                    boolean isHooked = tag.getBoolean("isHooked");
                    double distance = tag.getDouble("distance");
                    Vec3d hitPos = Vec3d.ZERO;

                    if (isHooked) {
                        long[] xList = tag.getLongArray("xList");
                        long[] yList = tag.getLongArray("yList");
                        long[] zList = tag.getLongArray("zList");

                        List<Long> posXList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(xList)));
                        List<Long> posYList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(yList)));
                        List<Long> posZList = new ArrayList<>(Arrays.asList(ArrayUtils.toObject(zList)));

                        int lastValue = posXList.size() - 1;


                        Vec3d prevHitPos = new Vec3d(Double.longBitsToDouble(posXList.get(lastValue)), Double.longBitsToDouble(posYList.get(lastValue)), Double.longBitsToDouble(posZList.get(lastValue)));

                        BlockHitResult hitResult = static_raycastBlock(world, entityPos, prevHitPos, entity, pos -> Objects.equals(pos, new BlockPos(entityPos.x, entityPos.y, entityPos.z)));

                        if (hitResult.getPos() != prevHitPos) {
                            posXList.add(Double.doubleToLongBits(hitResult.getPos().x));
                            posYList.add(Double.doubleToLongBits(hitResult.getPos().y));
                            posZList.add(Double.doubleToLongBits(hitResult.getPos().z));
                            distance -= hitResult.getPos().distanceTo(prevHitPos);
                            stack.getOrCreateNbt().putDouble("distance", distance);

                            HookPoint hookPoint;
                            hookPoint = Grapple.HOOK_POINT.create(world);
                            assert hookPoint != null;
                            hookPoint.setPos(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                            hookPoint.setColor(this.getColor(stack));
                            if (!world.isClient) {
                                world.spawnEntity(hookPoint);
                                NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
                                HookPoint hookPoint2 = (HookPoint) ((ServerWorld) world).getEntity(NbtHelper.toUuid(UUIDs.get(lastValue)));
                                if (hookPoint2 != null) {
                                    hookPoint2.setConnected(hookPoint.getUuidAsString());
                                }
                                hookPoint.setConnected(entity.getUuidAsString());
                            }


                            NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
                            UUIDs.add(NbtHelper.fromUuid(hookPoint.getUuid()));
                            stack.getOrCreateNbt().put("entities", UUIDs);
                            //System.out.println(stack.getOrCreateNbt().getList("entities", 11));


                        }
                        lastValue = posXList.size() - 1;


                        if (lastValue >= 1) {

                            prevHitPos = new Vec3d(Double.longBitsToDouble(posXList.get(lastValue - 1)), Double.longBitsToDouble(posYList.get(lastValue - 1)), Double.longBitsToDouble(posZList.get(lastValue - 1)));

                            hitResult = static_raycastBlock(world, entityPos, prevHitPos, entity, pos -> Objects.equals(pos, new BlockPos(entityPos.x, entityPos.y, entityPos.z)));


                            if (hitResult.getPos() == prevHitPos) {
                                Vec3d otherPoint = new Vec3d(Double.longBitsToDouble(posXList.get(lastValue)), Double.longBitsToDouble(posYList.get(lastValue)), Double.longBitsToDouble(posZList.get(lastValue)));

                                distance += otherPoint.distanceTo(prevHitPos);
                                stack.getOrCreateNbt().putDouble("distance", distance);

                                NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
                                if (!world.isClient) {
                                    HookPoint hookPoint = (HookPoint) ((ServerWorld) world).getEntity(NbtHelper.toUuid(UUIDs.get(lastValue)));
                                    assert hookPoint != null;
                                    hookPoint.kill();
                                    hookPoint = (HookPoint) ((ServerWorld) world).getEntity(NbtHelper.toUuid(UUIDs.get(lastValue - 1)));
                                    assert hookPoint != null;
                                    hookPoint.setConnected(entity.getUuidAsString());
                                }
                                UUIDs.remove(lastValue);
                                stack.getOrCreateNbt().put("entities", UUIDs);
                                //System.out.println(stack.getOrCreateNbt().getList("entities", 11));
                                posXList.remove(lastValue);
                                posYList.remove(lastValue);
                                posZList.remove(lastValue);
                            }

                        }
                        lastValue = posXList.size() - 1;
                        hitPos = new Vec3d(Double.longBitsToDouble(posXList.get(lastValue)), Double.longBitsToDouble(posYList.get(lastValue)), Double.longBitsToDouble(posZList.get(lastValue)));


                        if (!entity.isOnGround()) {
                            if(grappleToggle) {
                                grappleToggle = false;
                            }
                            ((LivingEntity) entity).setNoDrag(true);
                            entity.setNoGravity(true);

                            //entity.setVelocity(new Vec3d(entity.getVelocity().y,entity.getVelocity().x,entity.getVelocity().z));

                            Vec3d gotVelocity = entity.getVelocity();

                            Vec3d grappleVector = hitPos.subtract(entityPos);

                            grappleVector = RotationUtil.vecWorldToPlayer(grappleVector, GravityChangerAPI.getGravityDirection((PlayerEntity) entity));

                            Vec3d horizontalCorrection = grappleVector.normalize().multiply((.08), .08, (.08));

                            //horizontalCorrection = RotationUtil.vecWorldToPlayer(horizontalCorrection, GravityChangerAPI.getGravityDirection((PlayerEntity) entity));

                            Vec3d horizontalBoost = grappleVector.normalize().multiply(-Math.abs(gotVelocity.y) * .6666, .08, -Math.abs(gotVelocity.y) * .6666);
                            horizontalCorrection = horizontalCorrection.multiply(1, 0, 1);


                            if (!initalBoost && hitPos.distanceTo(entityPos) > distance) {
                                if (Math.abs(gotVelocity.x) < Math.abs(horizontalBoost.x)) {
                                    gotVelocity = (gotVelocity.add(-horizontalBoost.x, 0, 0));
                                }
                                if (Math.abs(gotVelocity.z) < Math.abs(horizontalBoost.z)) {
                                    gotVelocity = (gotVelocity.add(0, 0, -horizontalBoost.z));
                                }
                                stack.getOrCreateNbt().putBoolean("initalBoost", true);
                            }

                            if (hitPos.distanceTo(entityPos) > distance) {
                                gotVelocity = (gotVelocity.add(horizontalCorrection));

                            }
                            if (hitPos.distanceTo(entityPos) > distance && grappleVector.y >= 0) {
                                gotVelocity = (gotVelocity.add(new Vec3d(0, .08, 0)));
                            }

                            if (hitPos.distanceTo(entityPos) > distance && gotVelocity.y < 0) {
                                gotVelocity = ((new Vec3d(gotVelocity.x, gotVelocity.y / 2, gotVelocity.z)));
                            }

                            if (hitPos.distanceTo(entityPos) < distance + .1) {
                                gotVelocity = (gotVelocity.add(new Vec3d(0, -.08, 0)));
                            } else if (grappleVector.y <= 0) {
                                gotVelocity = (gotVelocity.add(new Vec3d(0, -.08, 0)));
                            }
                            // gotVelocity = new Vec3d(-gotVelocity.getX(),-gotVelocity.getY(),-gotVelocity.getZ());
                            entity.setVelocity(gotVelocity);
                            // entity.setVelocity(new Vec3d(gotVelocity.y,gotVelocity.x,gotVelocity.z));
                        }
                        entity.setVelocity(entity.getVelocity().multiply(.98));
                        //entity.setVelocity(RotationUtil.vecPlayerToWorld(entity.getVelocity().multiply(.98),GravityChangerAPI.getGravityDirection((PlayerEntity) entity).getOpposite() ));
                        //entity.setVelocity(new Vec3d(entity.getVelocity().y,entity.getVelocity().x,entity.getVelocity().z));

                        stack.getOrCreateNbt().putLongArray("xList", posXList);
                        stack.getOrCreateNbt().putLongArray("yList", posYList);
                        stack.getOrCreateNbt().putLongArray("zList", posZList);

                    } else {
                        if(!grappleToggle) {
                            ((LivingEntity) entity).setNoDrag(false);
                            entity.setNoGravity(false);
                            grappleToggle = true;
                        }
                    }

                    if (!isHooked) {

                        List<Long> emptyList = new ArrayList<>();
                        stack.getOrCreateNbt().putLongArray("xList", emptyList);
                        stack.getOrCreateNbt().putLongArray("yList", emptyList);
                        stack.getOrCreateNbt().putLongArray("zList", emptyList);
                    }

                    if (isHooked && entity.isOnGround()) {
                        if(!grappleToggle) {
                            ((LivingEntity) entity).setNoDrag(false);
                            entity.setNoGravity(false);
                            grappleToggle = true;
                        }
                        distance = hitPos.distanceTo(entityPos);

                        stack.getOrCreateNbt().putDouble("distance", distance);
                    }
                } else {
                    if(!grappleToggle) {
                        ((LivingEntity) entity).setNoDrag(false);
                        entity.setNoGravity(false);
                        grappleToggle = true;
                    }

                    NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);

                    for (NbtElement uuid : UUIDs) {
                        if (!world.isClient) {
                            HookPoint hookPoint = (HookPoint) ((ServerWorld) world).getEntity(NbtHelper.toUuid(uuid));
                            if (hookPoint != null) {
                                hookPoint.kill();
                            }
                        }
                    }

                    NbtList clearUUIDs = new NbtList();
                    stack.getOrCreateNbt().put("entities", clearUUIDs);

                    List<Long> emptyList = new ArrayList<>();
                    stack.getOrCreateNbt().putLongArray("xList", emptyList);
                    stack.getOrCreateNbt().putLongArray("yList", emptyList);
                    stack.getOrCreateNbt().putLongArray("zList", emptyList);
                    stack.getOrCreateNbt().putBoolean("isHookInUse", false);
                    stack.getOrCreateNbt().putBoolean("isHooked", false);
                }
        stack.getOrCreateNbt().putBoolean("grappleToggle", grappleToggle);

    }
    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
        boolean grappleToggle = stack.getOrCreateNbt().getBoolean("grappleToggle");
        if(!grappleToggle) {
            player.setNoDrag(false);
            player.setNoGravity(false);
            grappleToggle = true;
        }
        for (NbtElement uuid : UUIDs) {
            if (!player.world.isClient) {
                HookPoint hookPoint = (HookPoint) ((ServerWorld) player.world).getEntity(NbtHelper.toUuid(uuid));
                if(hookPoint != null) {
                    hookPoint.kill();
                }
            }
            if(!player.world.isClient) {
                NbtList clearUUIDs = new NbtList();
                stack.getOrCreateNbt().put("entities", clearUUIDs);
                List<Long> emptyList = new ArrayList<>();
                stack.getOrCreateNbt().putLongArray("xList", emptyList);
                stack.getOrCreateNbt().putLongArray("yList", emptyList);
                stack.getOrCreateNbt().putLongArray("zList", emptyList);

                stack.getOrCreateNbt().putBoolean("isHooked", false);
            }
        }
        stack.getOrCreateNbt().putBoolean("grappleToggle", grappleToggle);
        return false;
    }
    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
        player.setNoGravity(false);
        player.setNoDrag(false);
        for (NbtElement uuid : UUIDs) {
            if (!player.world.isClient) {
                HookPoint hookPoint = (HookPoint) ((ServerWorld) player.world).getEntity(NbtHelper.toUuid(uuid));
                if(hookPoint != null) {
                    hookPoint.kill();
                }
            }
            if(!player.world.isClient) {
                NbtList clearUUIDs = new NbtList();
                stack.getOrCreateNbt().put("entities", clearUUIDs);
                List<Long> emptyList = new ArrayList<>();
                stack.getOrCreateNbt().putLongArray("xList", emptyList);
                stack.getOrCreateNbt().putLongArray("yList", emptyList);
                stack.getOrCreateNbt().putLongArray("zList", emptyList);

                stack.getOrCreateNbt().putBoolean("isHooked", false);
            }
        }
        return false;
    }

    public static BlockHitResult static_raycastBlock(World world, Vec3d start, Vec3d end,Entity entity, Predicate<BlockPos> shouldSkip) {
        return BlockView.raycast(start, end, new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity), (ctx, pos) -> {
            if (shouldSkip.test(pos)) return null;
            BlockState state = world.getBlockState(pos);
            VoxelShape shape = ctx.getBlockShape(state, world, pos);
            return world.raycastBlock(start, end, pos, shape, state);
        }, (ctx)-> {
            Vec3d vec3d = ctx.getStart().subtract(ctx.getEnd());
            return BlockHitResult.createMissed(ctx.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), new BlockPos(ctx.getEnd()));
        });
    }

}
