package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;


import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoicePlayerInfo;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerInfoUpdatePacket extends TcpPlasmoPacket<PlayerInfoUpdatePacket> {

    private @MonotonicNonNull VoicePlayerInfo playerInfo;

    public PlayerInfoUpdatePacket() {

    }

    @Override
    public void encode(ByteBuf buf) {
        this.playerInfo.write(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.playerInfo = new VoicePlayerInfo(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handlePlayerInfoUpdatePacket(this);
    }

    public VoicePlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    public void setPlayerInfo(VoicePlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }
}
