package dev.minceraft.sonus.svc.adapter.connection;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcCipherCodec;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;
import java.util.UUID;

@NullMarked
public class SvcConnection {

    private final ISonusPlayer player;
    private final UUID secret = UUID.randomUUID();
    private final SvcCipherCodec cipher = new SvcCipherCodec(this.secret);
    // RemoteAddress will be set after first packet is received - usually at the construction of the connection
    private @MonotonicNonNull InetSocketAddress remoteAddress;

    public SvcConnection(ISonusPlayer player) {
        this.player = player;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public ISonusPlayer getPlayer() {
        return player;
    }

    public UUID getSecret() {
        return secret;
    }
}
