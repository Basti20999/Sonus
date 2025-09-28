package dev.minceraft.sonus.plasmo.protocol.tcp;


import dev.minceraft.sonus.plasmo.protocol.AbstractPlasmoPacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class TcpPlasmoPacket<T extends TcpPlasmoPacket<T>> extends AbstractPlasmoPacket<TcpHandler> {

    protected TcpPlasmoPacket() {
    }
}
