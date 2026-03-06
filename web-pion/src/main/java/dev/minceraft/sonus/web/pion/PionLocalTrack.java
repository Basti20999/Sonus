package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (4:58 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcLocalTrackSendData;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PionLocalTrack {

    private final PionPeer peer;
    private final int trackId;

    private final int sampleRate;
    private final short channels;

    PionLocalTrack(PionPeer peer, int trackId, int sampleRate, short channels) {
        this.peer = peer;
        this.trackId = trackId;
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public short getChannels() {
        return this.channels;
    }

    public void sendData(ByteBuf data, long durationNanos) {
        this.peer.ipc.send(new IpcLocalTrackSendData(this.peer.handlerId, this.trackId, data, durationNanos));
    }
}
