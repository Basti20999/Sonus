package dev.minceraft.sonus.web.protocol.packets.servicebound;
// Created by booky10 in Sonus (20:34 28.11.2025)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RoomCreatePacket extends WebsocketPacket {

    static final int MAX_ROOM_NAME_LENGTH = 32;
    static final int MAX_ROOM_PASSWORD_LENGTH = 32;

    private @MonotonicNonNull String name;
    private @Nullable String password;
    private boolean speakToOthers;
    private boolean listenToOthers;

    public RoomCreatePacket(String name, @Nullable String password, boolean speakToOthers, boolean listenToOthers) {
        this.name = name;
        this.password = password;
        this.speakToOthers = speakToOthers;
        this.listenToOthers = listenToOthers;
    }

    public RoomCreatePacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        Utf8String.write(buf, this.name);
        DataTypeUtil.writeNullable(buf, this.password, Utf8String::write);
        buf.writeBoolean(this.speakToOthers);
        buf.writeBoolean(this.listenToOthers);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.name = Utf8String.read(buf, MAX_ROOM_NAME_LENGTH);
        this.password = DataTypeUtil.readNullable(buf, ew ->
                Utf8String.read(ew, MAX_ROOM_PASSWORD_LENGTH));
        this.speakToOthers = buf.readBoolean();
        this.listenToOthers = buf.readBoolean();
        if (this.speakToOthers && this.listenToOthers) {
            throw new IllegalStateException("Tried creating passthrough room named " + this.name);
        }
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleRoomCreate(this);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Nullable String getPassword() {
        return this.password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public boolean isSpeakToOthers() {
        return this.speakToOthers;
    }

    public void setSpeakToOthers(boolean speakToOthers) {
        this.speakToOthers = speakToOthers;
    }

    public boolean isListenToOthers() {
        return this.listenToOthers;
    }

    public void setListenToOthers(boolean listenToOthers) {
        this.listenToOthers = listenToOthers;
    }
}
