package de.pianoman911.sonus.svcprotocol.data;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class SonusPlayerState {

    private final UUID playerId;
    private final String name;
    private final boolean disabled;
    private final boolean disconnected;
    @Nullable
    private final UUID groupId;

    public SonusPlayerState(UUID playerId, String name, boolean disabled, boolean disconnected, @Nullable UUID groupId) {
        this.playerId = playerId;
        this.name = name;
        this.disabled = disabled;
        this.disconnected = disconnected;
        this.groupId = groupId;
    }

    public SonusPlayerState(ByteBuf buf) {
        this.disabled = buf.readBoolean();
        this.disconnected = buf.readBoolean();
        this.playerId = new UUID(buf.readLong(), buf.readLong());
        this.name = Utf8String.read(buf);
        this.groupId = DataTypeUtil.readIf(buf, DataTypeUtil::readUniqueId);
    }

    public SonusPlayerState(JsonObject json) {
        this.playerId = UUID.fromString(json.get("playerId").getAsString());
        this.name = json.get("name").getAsString();
        this.disabled = json.get("disabled").getAsBoolean();
        this.disconnected = json.get("disconnected").getAsBoolean();
        if (json.has("groupId")) {
            this.groupId = UUID.fromString(json.get("groupId").getAsString());
        } else {
            this.groupId = null;
        }
    }

    public void encode(ByteBuf buf) {
        buf.writeBoolean(this.disabled);
        buf.writeBoolean(this.disconnected);
        buf.writeLong(this.playerId.getMostSignificantBits());
        buf.writeLong(this.playerId.getLeastSignificantBits());
        Utf8String.write(buf, this.name);
        DataTypeUtil.writeNullable(buf, this.groupId, DataTypeUtil::writeUniqueId);
    }

    public void encode(JsonObject json) {
        json.addProperty("playerId", this.playerId.toString());
        json.addProperty("name", this.name);
        json.addProperty("disabled", this.disabled);
        json.addProperty("disconnected", this.disconnected);
        if (this.groupId != null) {
            json.addProperty("groupId", this.groupId.toString());
        }
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isDisconnected() {
        return this.disconnected;
    }

    @Nullable
    public UUID getGroupId() {
        return this.groupId;
    }

    public boolean hasGroup() {
        return this.groupId != null;
    }

    @Override
    public String toString() {
        return "SonusPlayerState{" +
                "playerId=" + playerId +
                ", name='" + name + '\'' +
                ", disabled=" + disabled +
                ", disconnected=" + disconnected +
                ", groupId=" + groupId +
                '}';
    }
}
