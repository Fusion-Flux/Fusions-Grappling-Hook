package com.fusionflux.grapple.entity;

import com.fusionflux.grapple.client.packets.GrapplePackets;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class HookPoint extends Entity {

    public HookPoint(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
    }

    public static final TrackedData<String> CONNECTED_ENTITY = DataTracker.registerData(HookPoint.class, TrackedDataHandlerRegistry.STRING);

    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(CONNECTED_ENTITY, "null");
    }

    public String getConnected() {
        return getDataTracker().get(CONNECTED_ENTITY);
    }

    public void setConnected(String string) {
        this.getDataTracker().set(CONNECTED_ENTITY, string);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

        packet.writeVarInt(Registry.ENTITY_TYPE.getRawId(this.getType()))
                .writeUuid(this.getUuid())
                .writeVarInt(this.getId())
                .writeDouble(this.getX())
                .writeDouble(this.getY())
                .writeDouble(this.getZ())
                .writeByte(MathHelper.floor(this.getPitch() * 256.0F / 360.0F))
                .writeByte(MathHelper.floor(this.getYaw() * 256.0F / 360.0F));

        return ServerPlayNetworking.createS2CPacket(GrapplePackets.SPAWN_PACKET, packet);
    }
}
