package de.pianoman911.sonus.svcprotocol.meta;


import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class JoinedGroupSvcPacket extends SvcMetaPacket<JoinedGroupSvcPacket> implements ClientBound {

    private @Nullable UUID groupId;
    private boolean wrongPassword;

    public JoinedGroupSvcPacket() {
        super(SvcPluginChannels.JOINED_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeNullable(buf, this.groupId, DataTypeUtil::writeUniqueId);
        buf.writeBoolean(this.wrongPassword);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.groupId = DataTypeUtil.readIf(buf, DataTypeUtil::readUniqueId);
        this.wrongPassword = buf.readBoolean();
    }

    @Override
    public void encode(JsonObject json, int version) {
        if (this.groupId != null) {
            json.addProperty("groupId", this.groupId.toString());
        }
        json.addProperty("wrongPassword", this.wrongPassword);
    }

    @Override
    public void decode(JsonObject json) {
        this.groupId = json.has("groupId") ? UUID.fromString(json.get("groupId").getAsString()) : null;
        this.wrongPassword = json.get("wrongPassword").getAsBoolean();
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleJoinedGroupPacket(player, this);
    }

    public @Nullable UUID getGroupId() {
        return this.groupId;
    }

    public void setGroupId(@Nullable UUID groupId) {
        this.groupId = groupId;
    }

    public boolean isWrongPassword() {
        return this.wrongPassword;
    }

    public void setWrongPassword(boolean wrongPassword) {
        this.wrongPassword = wrongPassword;
    }

    @Override
    public String toString() {
        return "JoinedGroupSvcPacket{" +
                "groupId=" + groupId +
                ", wrongPassword=" + wrongPassword +
                '}';
    }
}
