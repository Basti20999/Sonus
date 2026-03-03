package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (5:02 PM 02.03.2026)

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.adapter.util.AudioMixer;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCOfferOptions;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCPeerConnectionState;
import dev.onvoid.webrtc.RTCRtpTransceiverDirection;
import dev.onvoid.webrtc.RTCRtpTransceiverInit;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.audio.CustomAudioSource;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@NullMarked
public final class RtcHandler implements PeerConnectionObserver, AudioTrackSink, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("WebRTC");

    private final RtcManager manager;
    private final WebSocketConnection signalConnection;
    private @MonotonicNonNull RTCPeerConnection peer;

    // audio output handling
    private final CustomAudioSource audioSource;
    private final AudioMixer mixer = new AudioMixer();
    private @MonotonicNonNull ScheduledFuture<?> ticker;

    // audio input handling
    private @Nullable AudioTrack microphoneTrack;
    private long sequenceNumber = 0L;

    public RtcHandler(RtcManager manager, WebSocketConnection signalConnection) {
        this.manager = manager;
        this.signalConnection = signalConnection;
        this.audioSource = new CustomAudioSource(manager.getClock());
    }

    public void disconnect(String ignoredReason) {
        this.close();
    }

    public void initialize(PeerConnectionFactory factory, ScheduledExecutorService scheduler) {
        // advertise sending output channel
        AudioTrack outputTrack = factory.createAudioTrack("output0", this.audioSource);
        this.peer.addTrack(outputTrack, List.of());

        // start ticking audio mixer
        this.startTicking(scheduler);
    }

    @Override
    public void onIceCandidate(RTCIceCandidate candidate) {
        this.signalConnection.sendPacket(new RtcIceCandidatePacket(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex));
    }

    @Override
    public void onAddStream(MediaStream stream) {
        if (stream.getVideoTracks().length != 0) {
            this.disconnect("expected no video track");
            return;
        }
        AudioTrack[] tracks = stream.getAudioTracks();
        if (tracks.length != 1) {
            this.disconnect("expected singular audio track");
            return;
        }
        this.updateMicTrack(tracks[0]);
    }

    @Override
    public void onRemoveStream(MediaStream stream) {
        if (stream.getVideoTracks().length != 0) {
            return;
        }
        AudioTrack[] tracks = stream.getAudioTracks();
        if (tracks.length == 1 && this.microphoneTrack == tracks[0]) {
            this.updateMicTrack(null);
        }
    }

    private void updateMicTrack(@Nullable AudioTrack track) {
        if (this.microphoneTrack != null) {
            this.microphoneTrack.removeSink(this);
            this.microphoneTrack = null;
        }
        if (track != null) {
            track.addSink(this);
            this.microphoneTrack = track;
        }
    }

    @Override
    public void onData(byte[] data, int bitsPerSample, int sampleRate, int channels, int frames) {
        // align with sonus codec
        data = this.manager.resampleAudio(data, sampleRate, channels);
//        VoiceActivityDetector TODO
        SonusAudio.Pcm audio = new SonusAudio.Pcm(data, this.sequenceNumber++);
        this.signalConnection.getPlayer().handleAudioInput(audio);
    }

    public void queueAudio(UUID channelId, short[] leftAudio, short[] rightAudio) {
        this.mixer.handle(channelId, leftAudio, rightAudio);
    }

    private void startTicking(ScheduledExecutorService scheduler) {
        if (this.ticker != null) {
            throw new IllegalStateException("Already started ticking");
        }
        this.ticker = scheduler.scheduleAtFixedRate(this::tickAudio,
                RtcConstants.FRAME_INTERVAL, RtcConstants.FRAME_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private void tickAudio() {
        if (!this.isConnected()) {
            this.mixer.clear();
            return;
        }

        byte[] data = this.mixer.tick(RtcConstants.FRAME_SIZE);
        if (data == null) {
            return; // check for actual audio
        }
        this.audioSource.pushAudio(data, RtcConstants.BITS_PER_SAMPLE,
                RtcConstants.SAMPLE_RATE, 2,
                RtcConstants.FRAME_SIZE);
    }

    public void handleRemoteOffer(RTCSdpType type, @Nullable String sdp) {
        this.peer.setRemoteDescription(new RTCSessionDescription(type, sdp), new SetSessionDescriptionObserver() {
            @Override
            public void onSuccess() {
                // construct answer
                RTCAnswerOptions opts = new RTCAnswerOptions();
                opts.voiceActivityDetection = false;
                RtcHandler.this.peer.createAnswer(opts, new CreateSessionDescriptionObserver() {
                    @Override
                    public void onSuccess(RTCSessionDescription description) {
                        // save answer locally
                        RtcHandler.this.peer.setLocalDescription(description, new SetSessionDescriptionObserver() {
                            @Override
                            public void onSuccess() {
                                // inform browser about answer
                                String type = description.sdpType.name().toLowerCase(Locale.ROOT);
                                RtcHandler.this.signalConnection.sendPacket(new RtcOfferPacket(type, description.sdp));
                            }

                            @Override
                            public void onFailure(String error) {
                                LOGGER.error("Failed to set local session description answer for {}: {}", RtcHandler.this.peer, error);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        LOGGER.error("Failed to create session description answer for {}: {}", RtcHandler.this.peer, error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                LOGGER.error("Failed to set remote session description for {}: {}", RtcHandler.this.peer, error);
            }
        });
    }

    public RTCPeerConnection getPeer() {
        return this.peer;
    }

    public void setPeer(RTCPeerConnection peer) {
        if (this.peer != null) {
            throw new IllegalStateException("Peer is already set");
        }
        this.peer = peer;
    }

    public boolean isConnected() {
        return this.peer.getConnectionState() == RTCPeerConnectionState.CONNECTED;
    }

    @Override
    public void close() {
        this.peer.close();
        this.mixer.close();
        this.audioSource.dispose();
        this.ticker.cancel(true);
    }
}
