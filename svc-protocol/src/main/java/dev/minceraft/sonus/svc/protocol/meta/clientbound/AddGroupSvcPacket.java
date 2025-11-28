package dev.minceraft.sonus.svc.protocol.meta.clientbound;

import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.data.SonusClientGroup;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class AddGroupSvcPacket extends SvcMetaPacket {

    private @MonotonicNonNull SonusClientGroup group;

    public AddGroupSvcPacket() {
        super(SvcPluginChannels.ADD_GROUP);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        this.group.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        this.group = new SonusClientGroup(buf);
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handleAddGroupPacket(this);
    }

    public SonusClientGroup getGroup() {
        return this.group;
    }

    public void setGroup(SonusClientGroup group) {
        this.group = group;
    }
}
