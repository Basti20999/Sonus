package dev.minceraft.sonus.plasmo.protocol.tcp.clientbound;


import dev.minceraft.sonus.plasmo.protocol.tcp.TcpHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoiceActivation;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ActivationRegisterPacket extends TcpPlasmoPacket<ActivationRegisterPacket> {

    private @MonotonicNonNull VoiceActivation activation;

    public ActivationRegisterPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        this.activation.write(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.activation = new VoiceActivation(buf);
    }

    @Override
    public void handle(TcpHandler handler) {
        handler.handleActivationRegisterPacket(this);
    }

    public VoiceActivation getActivation() {
        return this.activation;
    }

    public void setActivation(VoiceActivation activation) {
        this.activation = activation;
    }
}
