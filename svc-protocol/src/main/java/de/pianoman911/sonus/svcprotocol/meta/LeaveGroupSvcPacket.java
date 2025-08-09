package de.pianoman911.sonus.svcprotocol.meta;


import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LeaveGroupSvcPacket extends SvcMetaPacket<LeaveGroupSvcPacket> implements ServerBound {

    public LeaveGroupSvcPacket() {
        super(SvcPluginChannels.LEAVE_GROUP);
    }

    public LeaveGroupSvcPacket(ByteBuf buf) {
        super(SvcPluginChannels.LEAVE_GROUP);
    }

    public LeaveGroupSvcPacket(JsonObject json) {
        super(SvcPluginChannels.LEAVE_GROUP);
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void encode(JsonObject json, int version) {
    }

    @Override
    public void decode(JsonObject json) {
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handleLeaveGroupPacket(player, this);
    }
}
