package dev.quantumkya.berdlyhttp.block.entity;

import dev.quantumkya.berdlyhttp.BerdlyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("null")
public class DimensionalTransmitterBlockEntity extends BlockEntity {
    private String endpointUrl = "";
    private String httpMethod = "";
    private int cooldownTicks = -1;
    private boolean sendCoordinates = true;
    private boolean hasCustomConfig = false;

    public DimensionalTransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIMENSIONAL_TRANSMITTER_BE.get(), pos, state);
    }

    public String getEndpointUrl() {
        return (hasCustomConfig && !endpointUrl.isEmpty()) ? endpointUrl : BerdlyConfig.ENDPOINT_URL.get();
    }

    public String getRawEndpointUrl() {
        return endpointUrl.isEmpty() ? BerdlyConfig.ENDPOINT_URL.get() : endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl != null ? endpointUrl : "";
        this.hasCustomConfig = true;
        setChanged();
        syncToClient();
    }

    public String getHttpMethod() {
        return (hasCustomConfig && !httpMethod.isEmpty()) ? httpMethod : BerdlyConfig.HTTP_METHOD.get();
    }

    public String getRawHttpMethod() {
        return httpMethod.isEmpty() ? BerdlyConfig.HTTP_METHOD.get() : httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod != null ? httpMethod : "POST";
        this.hasCustomConfig = true;
        setChanged();
        syncToClient();
    }

    public int getCooldownTicks() {
        return (hasCustomConfig && cooldownTicks >= 0) ? cooldownTicks : BerdlyConfig.COOLDOWN_TICKS.get();
    }

    public int getRawCooldownTicks() {
        return cooldownTicks >= 0 ? cooldownTicks : BerdlyConfig.COOLDOWN_TICKS.get();
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
        this.hasCustomConfig = true;
        setChanged();
        syncToClient();
    }

    public boolean isSendCoordinates() {
        return hasCustomConfig ? sendCoordinates : BerdlyConfig.SEND_COORDINATES.get();
    }

    public boolean getRawSendCoordinates() {
        return hasCustomConfig ? sendCoordinates : BerdlyConfig.SEND_COORDINATES.get();
    }

    public void setSendCoordinates(boolean sendCoordinates) {
        this.sendCoordinates = sendCoordinates;
        this.hasCustomConfig = true;
        setChanged();
        syncToClient();
    }

    public boolean hasCustomConfig() {
        return hasCustomConfig;
    }

    public void updateAll(String url, String method, int cooldown, boolean sendCoords) {
        this.endpointUrl = url != null ? url : "";
        this.httpMethod = method != null ? method : "POST";
        this.cooldownTicks = cooldown;
        this.sendCoordinates = sendCoords;
        this.hasCustomConfig = true;
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        net.minecraft.world.level.Level lvl = this.level;
        if (lvl != null && !lvl.isClientSide()) {
            lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.hasCustomConfig = tag.getBoolean("HasCustomConfig");
        this.endpointUrl = tag.getString("EndpointUrl");
        this.httpMethod = tag.getString("HttpMethod");
        this.cooldownTicks = tag.contains("CooldownTicks") ? tag.getInt("CooldownTicks") : -1;
        this.sendCoordinates = tag.getBoolean("SendCoordinates");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("HasCustomConfig", this.hasCustomConfig);
        tag.putString("EndpointUrl", this.endpointUrl);
        tag.putString("HttpMethod", this.httpMethod);
        tag.putInt("CooldownTicks", this.cooldownTicks);
        tag.putBoolean("SendCoordinates", this.sendCoordinates);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
