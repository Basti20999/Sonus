package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minecraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class JoinGroupSvcPacket extends SvcMetaPacket<JoinGroupSvcPacket> implements ServerBound {

    private @MonotonicNonNull UUID groupId;
    private @Nullable String password;

    public JoinGroupSvcPacket() {
        super(SvcPluginChannels.SET_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.groupId);
        DataTypeUtil.writeNullable(buf, this.password, Utf8String::write);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.groupId = DataTypeUtil.readUniqueId(buf);
        this.password = DataTypeUtil.readIf(buf, b -> Utf8String.read(b, 512));
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("groupId", this.groupId.toString());
        if (this.password != null) {
            json.addProperty("password", this.password);
        }
    }

    @Override
    public void decode(JsonObject json) {
        this.groupId = UUID.fromString(json.get("groupId").getAsString());
        this.password = json.has("password") ?
                json.get("password").getAsString() : null;
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleJoinGroupPacket(player, this);
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public @Nullable String getPassword() {
        return this.password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }
}
