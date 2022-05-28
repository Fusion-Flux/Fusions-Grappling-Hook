package com.fusionflux.grapple;

import com.fusionflux.grapple.entity.HookPoint;
import com.fusionflux.grapple.items.GrappleItem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class Grapple implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("grapple");

	public static final String MODID = "grapple";

	public static final GrappleItem GRAPPLE = new GrappleItem(new QuiltItemSettings().group(ItemGroup.MISC).maxCount(1));

	public static Identifier id(String path) {
		return new Identifier(MODID, path);
	}

	public static final EntityType<HookPoint> HOOK_POINT = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("entitytesting", "hook_point"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HookPoint::new).dimensions(EntityDimensions.fixed(0f, 0f)).build()
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.ITEM, new Identifier("grapple", "grapple"), GRAPPLE);
		LOGGER.info("Hello to a rather Quilty world");
	}
}
