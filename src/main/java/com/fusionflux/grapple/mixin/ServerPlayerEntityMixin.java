package com.fusionflux.grapple.mixin;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.entity.HookPoint;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {


	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	@Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"))
	public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
		if(stack.isOf(Grapple.GRAPPLE)){
			this.setNoDrag(false);
			this.setNoGravity(false);

			NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);

			for (NbtElement uuid : UUIDs) {
				if (!this.world.isClient) {
					HookPoint hookPoint = (HookPoint) ((ServerWorld) this.world).getEntity(NbtHelper.toUuid(uuid));
					if(hookPoint != null) {
						hookPoint.kill();
					}
				}
			}
			if(!this.world.isClient) {
				NbtList clearUUIDs = new NbtList();
				stack.getOrCreateNbt().put("entities", clearUUIDs);
				List<Long> emptyList = new ArrayList<>();
				stack.getOrCreateNbt().putLongArray("xList", emptyList);
				stack.getOrCreateNbt().putLongArray("yList", emptyList);
				stack.getOrCreateNbt().putLongArray("zList", emptyList);

				stack.getOrCreateNbt().putBoolean("isHooked", false);
			}

		}
	}


}
