package dev.minceraft.sonus.plasmo.protocol.tcp.serverbound;

import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerStatePacket<T extends PlayerStatePacket<T>> extends TcpPlasmoPacket<T> {

    private boolean voiceDisabled;
    private boolean microphoneMuted;

    public PlayerStatePacket() {

    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(this.voiceDisabled);
        buf.writeBoolean(this.microphoneMuted);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.voiceDisabled = buf.readBoolean();
        this.microphoneMuted = buf.readBoolean();
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handlePlayerStatePacket(this);
    }

    public boolean isVoiceDisabled() {
        return this.voiceDisabled;
    }

    public void setVoiceDisabled(boolean voiceDisabled) {
        this.voiceDisabled = voiceDisabled;
    }

    public boolean isMicrophoneMuted() {
        return this.microphoneMuted;
    }

    public void setMicrophoneMuted(boolean microphoneMuted) {
        this.microphoneMuted = microphoneMuted;
    }
}
