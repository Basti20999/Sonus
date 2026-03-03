package dev.minceraft.sonus.web.protocol.packets.commonbound;
// Created by booky10 in Sonus (5:22 PM 02.03.2026)

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import dev.minceraft.sonus.common.protocol.util.VarInt;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RtcIceCandidatePacket extends WebSocketPacket {

    private @Nullable String sdp;
    private @Nullable String sdpMid;
    private @Nullable Integer sdpMLineIndex;

    public RtcIceCandidatePacket() {
    }

    public RtcIceCandidatePacket(@Nullable String sdp, @Nullable String sdpMid, @Nullable Integer sdpMLineIndex) {
        this.sdp = sdp;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        DataTypeUtil.writeNullable(buf, this.sdp, Utf8String::write);
        DataTypeUtil.writeNullable(buf, this.sdpMid, Utf8String::write);
        DataTypeUtil.writeNullable(buf, this.sdpMLineIndex, VarInt::write);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        this.sdp = DataTypeUtil.readNullable(buf, ew -> Utf8String.read(ew, 256));
        this.sdpMid = DataTypeUtil.readNullable(buf, ew -> Utf8String.read(ew, 32));
        this.sdpMLineIndex = DataTypeUtil.readNullable(buf, VarInt::read);
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleRtcIceCandidate(this);
    }

    public @Nullable String getSdp() {
        return this.sdp;
    }

    public void setSdp(@Nullable String sdp) {
        this.sdp = sdp;
    }

    public @Nullable String getSdpMid() {
        return this.sdpMid;
    }

    public void setSdpMid(@Nullable String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public @Nullable Integer getSdpMLineIndex() {
        return this.sdpMLineIndex;
    }

    public void setSdpMLineIndex(@Nullable Integer sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
