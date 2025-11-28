package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LeaveGroupSvcPacket extends SvcMetaPacket {

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
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
    }

    @Override
    public void encode(JsonObject json) {
    }

    @Override
    public void decode(JsonObject json) {
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleLeaveGroupPacket(this);
    }
}
