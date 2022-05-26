package com.fusionflux.grapple.mixin;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.entity.HookPoint;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow @Final public PlayerEntity player;

    //@Shadow @Final public PlayerEntity player;
//
	@Inject(method = "remove", at = @At("HEAD"))
	public void remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory, CallbackInfoReturnable<Integer> cir) {
//
		ItemStack stack = this.player.currentScreenHandler.getCursorStack();
		if(stack.isOf(Grapple.GRAPPLE)){
            boolean grappleToggle = stack.getOrCreateNbt().getBoolean("grappleToggle");
            if(!grappleToggle) {
                player.setNoDrag(false);
                player.setNoGravity(false);
                grappleToggle = true;
            }
//
			NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);
//
			for (NbtElement uuid : UUIDs) {
				if (!player.world.isClient) {
					HookPoint hookPoint = (HookPoint) ((ServerWorld) player.world).getEntity(NbtHelper.toUuid(uuid));
					if(hookPoint != null) {
						hookPoint.kill();
					}
				}
			}
			if(!player.world.isClient) {
				NbtList clearUUIDs = new NbtList();
				stack.getOrCreateNbt().put("entities", clearUUIDs);
				List<Long> emptyList = new ArrayList<>();
				stack.getOrCreateNbt().putLongArray("xList", emptyList);
				stack.getOrCreateNbt().putLongArray("yList", emptyList);
				stack.getOrCreateNbt().putLongArray("zList", emptyList);
//
				stack.getOrCreateNbt().putBoolean("isHooked", false);
			}
            stack.getOrCreateNbt().putBoolean("grappleToggle", grappleToggle);
		}
//
//
//
	}
    @Inject(method = "dropSelectedItem", at = @At("HEAD"))
    public void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = this.getMainHandStack();
        if(stack.isOf(Grapple.GRAPPLE)){
            player.setNoDrag(false);
            player.setNoGravity(false);

            NbtList UUIDs = stack.getOrCreateNbt().getList("entities", 11);

            for (NbtElement uuid : UUIDs) {
                if (!player.world.isClient) {
                    HookPoint hookPoint = (HookPoint) ((ServerWorld) player.world).getEntity(NbtHelper.toUuid(uuid));
                    if(hookPoint != null) {
                        hookPoint.kill();
                    }
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
        //return itemStack.isEmpty() ? ItemStack.EMPTY : this.removeStack(this.selectedSlot, entireStack ? itemStack.getCount() : 1);
    }

}
