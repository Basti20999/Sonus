package dev.minceraft.sonus.plasmo.protocol.udp;

import dev.minceraft.sonus.plasmo.protocol.AbstractPlasmoPacket;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class UdpPlasmoPacket <T extends UdpPlasmoPacket<T>> extends AbstractPlasmoPacket<UdpHandler> {

    protected UdpPlasmoPacket() {
    }
}
