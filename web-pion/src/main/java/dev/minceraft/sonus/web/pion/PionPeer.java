package dev.minceraft.sonus.web.pion;
// Created by booky10 in Sonus (3:23 PM 06.03.2026)

import dev.minceraft.sonus.web.pion.ipc.IpcConnection;
import dev.minceraft.sonus.web.pion.ipc.IpcError;
import dev.minceraft.sonus.web.pion.ipc.IpcHandler;
import dev.minceraft.sonus.web.pion.ipc.commonbound.IpcPeerAddIceCandidate;
import dev.minceraft.sonus.web.pion.ipc.commonbound.IpcPeerSdp;
import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcApiAllocatePeer;
import dev.minceraft.sonus.web.pion.ipc.pionbound.IpcPeerClose;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerError;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerOnConnectionStateChange;
import dev.minceraft.sonus.web.pion.ipc.sonusbound.IpcPeerOnIceConnectionStateChange;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public final class PionPeer implements AutoCloseable {

    private final IpcConnection ipc;
    private final int handlerId;
    private final IpcHandler handler;

    private IceConnectionState iceConnectionState = IceConnectionState.UNKNOWN;
    private PeerConnectionState connectionState = PeerConnectionState.UNKNOWN;

    PionPeer(
            IpcConnection ipc,
            List<PionApi.IceServer> iceServers, PionApi.BundlePolicy bundlePolicy,
            PionPeer.Callback callback, String id
    ) {
        this.ipc = ipc;

        this.handler = message -> {
            switch (message) {
                case IpcPeerAddIceCandidate msg ->
                        callback.onIceCandidate(msg.getCandidate(), msg.getSdpMid(), msg.getSdpMLineIndex());
                case IpcPeerSdp msg -> callback.onAnswerCreated(msg.getSdp());
                case IpcPeerOnIceConnectionStateChange msg -> {
                    callback.onIceConnectionStateChange(msg.getState());
                    this.iceConnectionState = msg.getState();
                }
                case IpcPeerOnConnectionStateChange msg -> {
                    callback.onConnectionStateChange(msg.getState());
                    this.connectionState = msg.getState();
                }
                case IpcPeerError msg -> callback.onError(new IpcError(msg.getError()));
                default -> {
                }
            }
        };
        this.handlerId = ipc.registerHandler(this.handler);
        ipc.send(new IpcApiAllocatePeer(this.handlerId, iceServers, bundlePolicy, id));
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
