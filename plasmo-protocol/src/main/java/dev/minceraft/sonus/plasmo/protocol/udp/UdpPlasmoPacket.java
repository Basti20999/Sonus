package dev.minceraft.sonus.plasmo.protocol.udp;

import dev.minceraft.sonus.plasmo.protocol.AbstractPlasmoPacket;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public abstract class UdpPlasmoPacket<T extends UdpPlasmoPacket<T>> extends AbstractPlasmoPacket<UdpHandler> {

    private @MonotonicNonNull UUID secret;
    private long timestamp;

    protected UdpPlasmoPacket() {
    }

    public UUID getSecret() {
        return this.secret;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
