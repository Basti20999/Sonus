package dev.minceraft.sonus.protocol.meta.agentbound;
// Created by booky10 in Sonus (18:53 17.11.2025)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

// TODO fire this packet
@NullMarked
public class PlayerConnectionStateMessage implements IMetaMessage {

    private @MonotonicNonNull UUID playerId;
    private boolean connected;

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        buf.writeBoolean(this.connected);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.playerId = DataTypeUtil.readUniqueId(buf);
        this.connected = buf.readBoolean();
    }

    @Override
    public void handle(IMetaHandler handler) {
        handler.handlePlayerConnectionState(this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
