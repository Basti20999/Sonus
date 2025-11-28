package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:39 28.11.2025)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class ConnectedPacket extends WebsocketPacket {

    private @MonotonicNonNull UUID playerId;
    private @MonotonicNonNull Component username;

    public ConnectedPacket(UUID playerId, Component username) {
        this.playerId = playerId;
        this.username = username;
    }

    public ConnectedPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        DataTypeUtil.writeComponentJson(buf, this.username);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.failClientboundDecode();
        this.playerId = DataTypeUtil.readUniqueId(buf);
        this.username = DataTypeUtil.readComponentJson(buf);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleConnected(this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public Component getUsername() {
        return this.username;
    }

    public void setUsername(Component username) {
        this.username = username;
    }
}
