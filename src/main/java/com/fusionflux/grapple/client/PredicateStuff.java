package com.fusionflux.grapple.client;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.items.GrappleItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.util.TriConsumer;
@Environment(EnvType.CLIENT)
public class PredicateStuff {
    public static void init(TriConsumer<Item, Identifier, UnclampedModelPredicateProvider> modelPredicateProviderFactory) {
        modelPredicateProviderFactory.accept(Registry.ITEM.get(Grapple.id("grapple")), new Identifier("hooked"),
                (itemStack, clientWorld, livingEntity, i) -> GrappleItem.getIsHooked(itemStack));
    }
}
