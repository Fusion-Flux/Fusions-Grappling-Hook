package com.fusionflux.grapple;

import com.fusionflux.grapple.client.packets.GrapplePackets;
import com.fusionflux.grapple.entity.HookPoint;
import com.fusionflux.grapple.items.GrappleItem;
import com.mojang.datafixers.types.templates.Hook;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.LeashKnotEntityRenderer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Grapple implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("modid");

	public static final GrappleItem GRAPPLE = new GrappleItem(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1).fireproof());

	public static final EntityType<HookPoint> HOOK_POINT = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("entitytesting", "hook_point"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HookPoint::new).dimensions(EntityDimensions.fixed(1f, 1f)).build()
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		GrapplePackets.registerPackets();
		Registry.register(Registry.ITEM, new Identifier("grapple", "grapple"), GRAPPLE);
		LOGGER.info("Hello Fabric world!");
	}
}
