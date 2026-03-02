package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (5:02 PM 02.03.2026)

import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@NullMarked
public final class RtcHandler implements PeerConnectionObserver, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("WebRTC");

    private final WebSocketConnection signalConnection;
    private @MonotonicNonNull RTCPeerConnection peer;

    public RtcHandler(WebSocketConnection signalConnection) {
        this.signalConnection = signalConnection;
    }

    public void prepareOffer() {
        this.peer.createOffer(new RTCOfferOptions(), new CreateSessionDescriptionObserver() {
            @Override
            public void onSuccess(RTCSessionDescription description) {
                RtcHandler.this.peer.setLocalDescription(description, new SetSessionDescriptionObserver() {
                    @Override
                    public void onSuccess() {
                        String type = description.sdpType.name().toLowerCase(Locale.ROOT);
                        RtcHandler.this.signalConnection.sendPacket(new RtcOfferPacket(type, description.sdp));
                    }

                    @Override
                    public void onFailure(String error) {
                        LOGGER.error("Failed to set local session description for {}: {}", RtcHandler.this.peer, error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                LOGGER.error("Failed to create local session description for {}: {}", RtcHandler.this.peer, error);
            }
        });
    }

    @Override
    public void onIceCandidate(RTCIceCandidate candidate) {
        this.signalConnection.sendPacket(new RtcIceCandidatePacket(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex));
    }

    public void handleRemoteOffer(RTCSdpType type, @Nullable String sdp) {
        this.peer.setRemoteDescription(new RTCSessionDescription(type, sdp), new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                // NO-OP
            }

            @Override
            public void onFailure(String error) {
                LOGGER.error("Failed to set remote session description for {}: {}", RtcHandler.this.peer, error);
            }
        });
    }

    public @MonotonicNonNull RTCPeerConnection getPeer() {
        return this.peer;
    }

    public void setPeer(RTCPeerConnection peer) {
        this.peer = peer;
    }

    @Override
    public void close() {
        this.peer.close();
    }
}
