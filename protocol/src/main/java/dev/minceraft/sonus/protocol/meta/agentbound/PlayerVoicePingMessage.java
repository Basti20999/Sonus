package dev.minceraft.sonus.protocol.meta.agentbound;
// Created for Sonus - Voice chat ping forwarding (service → agent)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class PlayerVoicePingMessage implements IMetaMessage {

    private @MonotonicNonNull UUID playerId;
    /** Round-trip voice ping in milliseconds, or -1 when unknown. */
    private long pingMs;

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        buf.writeLong(this.pingMs);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.playerId = DataTypeUtil.readUniqueId(buf);
        this.pingMs = buf.readLong();
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handlePlayerVoicePing(this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public long getPingMs() {
        return this.pingMs;
    }

    public void setPingMs(long pingMs) {
        this.pingMs = pingMs;
    }
}
