package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (3:23 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcConnection;
import dev.minceraft.sonus.web.pion.ipc.IpcError;
import dev.minceraft.sonus.web.pion.ipc.IpcMessage;
import dev.minceraft.sonus.web.pion.ipc.commonbound.IpcPeerAddIceCandidate;
import dev.minceraft.sonus.web.pion.ipc.commonbound.IpcPeerSdp;
import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcApiAllocatePeer;
import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcPeerClose;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerError;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerOnAudioTrack;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerOnConnectionStateChange;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerOnIceConnectionStateChange;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcRemoteTrackOnData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class PionPeer implements AutoCloseable {

    final IpcConnection ipc;
    final int handlerId;

    private final PionPeer.Callback callback;
    private @Nullable PionRemoteTrack remoteTrack = null;
    private PionRemoteTrack.@Nullable Callback remoteTrackCallback;

    private IceConnectionState iceConnectionState = IceConnectionState.UNKNOWN;
    private PeerConnectionState connectionState = PeerConnectionState.UNKNOWN;

    PionPeer(
            IpcConnection ipc, PionPeer.Callback callback,
            List<PionApi.IceServer> iceServers, PionApi.BundlePolicy bundlePolicy, String id
    ) {
        this.ipc = ipc;
        this.callback = callback;
        this.handlerId = ipc.registerHandler(this::handleIpcMessage);
        ipc.send(new IpcApiAllocatePeer(this.handlerId, iceServers, bundlePolicy, id));
    }

    private void handleIpcMessage(IpcMessage message) {
        switch (message) {
            case IpcPeerAddIceCandidate msg ->
                    this.callback.onIceCandidate(msg.getCandidate(), msg.getSdpMid(), msg.getSdpMLineIndex());
            case IpcPeerSdp msg -> this.callback.onAnswerCreated(msg.getSdp());
            case IpcPeerOnIceConnectionStateChange msg -> {
                this.callback.onIceConnectionStateChange(msg.getState());
                this.iceConnectionState = msg.getState();
            }
            case IpcPeerOnConnectionStateChange msg -> {
                this.callback.onConnectionStateChange(msg.getState());
                this.connectionState = msg.getState();
            }
            case IpcPeerOnAudioTrack msg -> {
                switch (msg.getType()) {
                    case REMOTE -> {
                        this.remoteTrack = new PionRemoteTrack(msg.getTrackId(), msg.getSampleRate(), msg.getChannels());
                        this.remoteTrackCallback = this.callback.onRemoteAudioTrack(this.remoteTrack);
                    }
                    case LOCAL -> {
                        PionLocalTrack track = new PionLocalTrack(this, msg.getTrackId(), msg.getSampleRate(), msg.getChannels());
                        this.callback.onLocalAudioTrack(track);
                    }
                }
            }
            case IpcPeerError msg -> {
                IpcError error = new IpcError(msg.getError());
                this.callback.onError(error);
            }
            case IpcRemoteTrackOnData msg -> {
                try {
                    if (this.remoteTrack != null
                            && this.remoteTrack.trackId == msg.getTrackId()
                            && this.remoteTrackCallback != null) {
                        this.remoteTrackCallback.onData(msg.getData(), msg.getDurationNanos());
                    }
                } finally {
                    msg.getData().release();
                }
            }
            default -> {
            }
        }
    }

    public void addIceCandidate(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex) {
        this.ipc.send(new IpcPeerAddIceCandidate(this.handlerId, candidate, sdpMid, sdpMLineIndex));
    }

    public void handleOffer(String sdp) {
        this.ipc.send(new IpcPeerSdp(this.handlerId, sdp));
    }

    @Override
    public void close() {
        this.ipc.send(new IpcPeerClose(this.handlerId));
        this.ipc.unregisterHandler(this.handlerId);
    }

    public IceConnectionState getIceConnectionState() {
        return this.iceConnectionState;
    }

    public PeerConnectionState getConnectionState() {
        return this.connectionState;
    }

    public interface Callback {

        void onIceCandidate(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex);

        void onAnswerCreated(String sdp);

        void onIceConnectionStateChange(IceConnectionState state);

        void onConnectionStateChange(PeerConnectionState state);

        PionRemoteTrack.@Nullable Callback onRemoteAudioTrack(PionRemoteTrack track);

        void onLocalAudioTrack(PionLocalTrack track);

        void onError(Error error);
    }
}
