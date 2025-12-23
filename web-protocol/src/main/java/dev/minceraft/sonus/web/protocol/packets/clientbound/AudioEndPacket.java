package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (00:56 23.12.2025)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class AudioEndPacket extends WebSocketPacket {

    private @MonotonicNonNull UUID channelId;
    private @MonotonicNonNull UUID senderId;

    public AudioEndPacket(UUID channelId, UUID senderId) {
        this.channelId = channelId;
        this.senderId = senderId;
    }

    public AudioEndPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        boolean senderIsChannel = this.senderId.equals(this.channelId);
        buf.writeBoolean(senderIsChannel);
        DataTypeUtil.writeUniqueId(buf, this.channelId);
        if (!senderIsChannel) {
            DataTypeUtil.writeUniqueId(buf, this.senderId);
        }
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        boolean senderIsChannel = buf.readBoolean();
        this.channelId = DataTypeUtil.readUniqueId(buf);
        if (senderIsChannel) {
            this.senderId = this.channelId;
        } else {
            this.senderId = DataTypeUtil.readUniqueId(buf);
        }
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleAudioEnd(this);
    }

    public UUID getChannelId() {
        return this.channelId;
    }

    public void setChannelId(UUID channelId) {
        this.channelId = channelId;
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }
}
