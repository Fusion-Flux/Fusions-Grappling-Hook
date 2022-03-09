package com.fusionflux.grapple.client.packets;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class GrapplePackets {
    public static final Identifier SPAWN_PACKET = new Identifier("grapple", "spawn_packet");

    @Environment(EnvType.CLIENT)
    public static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SPAWN_PACKET, GrapplePackets::onEntitySpawn);
    }
    @Environment(EnvType.CLIENT)
    public static void onEntitySpawn(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        EntityType<?> type = Registry.ENTITY_TYPE.get(buf.readVarInt());
        UUID entityUUID = buf.readUuid();
        int entityID = buf.readVarInt();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float pitch = (buf.readByte() * 360) / 256.0F;
        float yaw = (buf.readByte() * 360) / 256.0F;
        ClientWorld world = MinecraftClient.getInstance().world;
        Entity entity = type.create(world);
        client.execute(() -> {
            if (entity != null) {
                entity.updatePosition(x, y, z);
                entity.updateTrackedPosition(x, y, z);
                entity.setPitch(pitch);
                entity.setYaw(yaw);
                entity.setId(entityID);
                entity.setUuid(entityUUID);
                assert world != null;
                world.addEntity(entityID, entity);
            }
        });
    }

}
