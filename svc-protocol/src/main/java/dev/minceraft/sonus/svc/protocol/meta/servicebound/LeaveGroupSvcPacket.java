package dev.minceraft.sonus.svc.protocol.meta.servicebound;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class LeaveGroupSvcPacket extends SvcMetaPacket {

    public LeaveGroupSvcPacket() {
        super(SvcPluginChannels.LEAVE_GROUP);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        // NO-OP
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        // NO-OP
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleLeaveGroupPacket(this);
    }
}
