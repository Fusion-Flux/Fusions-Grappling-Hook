package com.fusionflux.grapple.mixin;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.accessors.Accessors;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLookup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(World.class)
public abstract class WorldMixin implements Accessors {

	@Shadow protected abstract EntityLookup<Entity> getEntityLookup();

	@Override
	@Nullable
	public Entity getEntity(UUID uuid) {
		return this.getEntityLookup().get(uuid);
	}
}
