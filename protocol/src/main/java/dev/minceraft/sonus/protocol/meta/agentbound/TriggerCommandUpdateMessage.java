package dev.minceraft.sonus.protocol.meta.agentbound;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class TriggerCommandUpdateMessage implements IMetaMessage {

    private @MonotonicNonNull UUID playerId;

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.playerId = DataTypeUtil.readUniqueId(buf);
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handleTriggerCommandUpdate(this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }
}
