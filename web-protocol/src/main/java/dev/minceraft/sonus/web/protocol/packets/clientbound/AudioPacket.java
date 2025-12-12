package dev.minceraft.sonus.web.protocol.packets.clientbound;
// Created by booky10 in Sonus (20:32 28.11.2025)

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.Vec3d;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class AudioPacket extends WebSocketPacket {

    private static final byte FLAG_HAS_CATEGORY = 1 << 0;
    private static final byte FLAG_HAS_POSITION = 1 << 1;
    private static final byte FLAG_SENDER_IS_CHANNEL = 1 << 2;

    private @MonotonicNonNull UUID channelId;
    private @MonotonicNonNull UUID senderId;
    private SonusAudio.@MonotonicNonNull Opus audio;
    private @Nullable UUID categoryId;
    private @Nullable Vec3d position;

    public AudioPacket(
            UUID channelId, UUID senderId,
            SonusAudio.Opus audio, @Nullable UUID categoryId,
            @Nullable Vec3d position
    ) {
        this.channelId = channelId;
        this.senderId = senderId;
        this.audio = audio;
        this.categoryId = categoryId;
        this.position = position;
    }

    public AudioPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        boolean senderIsChannel = this.senderId.equals(this.channelId);
        buf.writeByte(0
                | (this.categoryId != null ? FLAG_HAS_CATEGORY : 0)
                | (this.position != null ? FLAG_HAS_POSITION : 0)
                | (senderIsChannel ? FLAG_SENDER_IS_CHANNEL : 0));
        DataTypeUtil.writeUniqueId(buf, this.channelId);
        if (!senderIsChannel) {
            DataTypeUtil.writeUniqueId(buf, this.senderId);
        }
        DataTypeUtil.VAR_INT.writeByteArray(buf, this.audio.opus());
        if (this.categoryId != null) {
            DataTypeUtil.writeUniqueId(buf, this.categoryId);
        }
        if (this.position != null) {
            Vec3d.encode(buf, this.position);
        }
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        short flags = buf.readUnsignedByte();
        this.channelId = DataTypeUtil.readUniqueId(buf);
        if ((flags & FLAG_SENDER_IS_CHANNEL) != 0) {
            this.senderId = this.channelId;
        } else {
            this.senderId = DataTypeUtil.readUniqueId(buf);
        }
        this.audio = new SonusAudio.Opus(DataTypeUtil.VAR_INT.readByteArray(buf), 0L);
        if ((flags & FLAG_HAS_CATEGORY) != 0) {
            this.categoryId = DataTypeUtil.readUniqueId(buf);
        }
        if ((flags & FLAG_HAS_POSITION) != 0) {
            this.position = Vec3d.decode(buf);
        }
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleAudio(this);
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

    public SonusAudio.Opus getAudio() {
        return this.audio;
    }

    public void setAudio(SonusAudio.Opus audio) {
        this.audio = audio;
    }

    public @Nullable UUID getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(@Nullable UUID categoryId) {
        this.categoryId = categoryId;
    }

    public @Nullable Vec3d getPosition() {
        return this.position;
    }

    public void setPosition(@Nullable Vec3d position) {
        this.position = position;
    }
}
