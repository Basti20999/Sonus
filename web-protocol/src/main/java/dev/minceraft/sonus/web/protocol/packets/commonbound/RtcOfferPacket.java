package dev.minceraft.sonus.web.protocol.packets.commonbound;
// Created by booky10 in Sonus (5:22 PM 02.03.2026)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RtcOfferPacket extends WebSocketPacket {

    private @MonotonicNonNull String type;
    private @Nullable String sdp;

    public RtcOfferPacket() {
    }

    public RtcOfferPacket(String type, @Nullable String sdp) {
        this.type = type;
        this.sdp = sdp;
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        Utf8String.write(buf, this.type);
        DataTypeUtil.writeNullable(buf, this.sdp, Utf8String::write);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.type = Utf8String.read(buf, 16);
        this.sdp = DataTypeUtil.readNullable(buf, ew -> Utf8String.read(ew, 64));
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleRtcOffer(this);
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public @Nullable String getSdp() {
        return this.sdp;
    }

    public void setSdp(@Nullable String sdp) {
        this.sdp = sdp;
    }
}
