package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (3:23 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PionPeer implements AutoCloseable {

    PionPeer(PionPeer.Callback callback) {
    }

    public void addIceCandidate(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex) {
        // FIXME
    }

    /**
     * @return answer sdp
     */
    public String handleOffer(String sdp) {
        // FIXME
    }

    @Override
    public void close() {
        // FIXME
    }

    public IceConnectionState getIceConnectionState() {

    }

    public PeerConnectionState getConnectionState() {

    }

    public interface Callback {

        void onIceCandidate(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex);

        void onIceConnectionStateChange(IceConnectionState state);

        void onConnectionStateChange(PeerConnectionState state);

        PionRemoteTrack.@Nullable Callback onRemoteAudioTrack(PionRemoteTrack track);

        void onLocalAudioTrack(PionLocalTrack track);

        void onError(Error error);
    }
}
