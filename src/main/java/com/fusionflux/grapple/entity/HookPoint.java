package com.fusionflux.grapple.entity;

import com.fusionflux.grapple.Grapple;
import com.fusionflux.grapple.client.packets.GrapplePackets;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Objects;
import java.util.UUID;

public class HookPoint extends Entity {

    public HookPoint(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
        this.ignoreCameraFrustum = true;
    }

    public static final TrackedData<String> CONNECTED_ENTITY = DataTracker.registerData(HookPoint.class, TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> COLOR = DataTracker.registerData(HookPoint.class, TrackedDataHandlerRegistry.INTEGER);

@Override
    public boolean shouldRender(double distance) {
    return  true;
    }
    @Override
    protected void initDataTracker() {
        this.getDataTracker().startTracking(CONNECTED_ENTITY, "null");
        this.getDataTracker().startTracking(COLOR, 0);
    }

@Override
public void tick() {
    if(!this.world.isClient) {
        if(!Objects.equals(getConnected(), "null")) {
            Entity hookPoint = ((ServerWorld) world).getEntity(UUID.fromString(getConnected()));
            if (hookPoint == null) {
                this.kill();
            }
            if (hookPoint instanceof PlayerEntity player) {
                if(player.getMainHandStack().getItem() != Grapple.GRAPPLE){
                    if(player.getOffHandStack().getItem() != Grapple.GRAPPLE){
                        this.kill();
                    }
                }
            }
        }else{
            this.kill();
        }
    }
}

    public String getConnected() {
        return getDataTracker().get(CONNECTED_ENTITY);
    }

    public void setConnected(String string) {
        this.getDataTracker().set(CONNECTED_ENTITY, string);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
            this.setColor(nbt.getInt("color"));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("color", this.getColor());
    }

    public Integer getColor() {
        return getDataTracker().get(COLOR);
    }

    public void setColor(Integer color) {
        this.getDataTracker().set(COLOR, color);
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
