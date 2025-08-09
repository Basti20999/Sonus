package de.pianoman911.sonus.svcprotocol.meta;


import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class RemoveGroupSvcPacket extends SvcMetaPacket<RemoveGroupSvcPacket> implements ClientBound {

    private @MonotonicNonNull UUID groupId;

    public RemoveGroupSvcPacket() {
        super(SvcPluginChannels.REMOVE_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.groupId);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.groupId = DataTypeUtil.readUniqueId(buf);
    }

    @Override
    public void encode(JsonObject json, int version) {
        json.addProperty("groupId", this.groupId.toString());
    }

    @Override
    public void decode(JsonObject json) {
        this.groupId = UUID.fromString(json.get("groupId").getAsString());
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleRemoveGroupPacket(player, this);
    }

    public UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
}
