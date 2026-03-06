package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (5:02 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.natives.OpusNativesLoader;
import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.adapter.util.AudioMixer;
import dev.minceraft.sonus.web.pion.ipc.model.IceConnectionState;
import dev.minceraft.sonus.web.pion.ipc.model.PeerConnectionState;
import dev.minceraft.sonus.web.pion.PionLocalTrack;
import dev.minceraft.sonus.web.pion.PionPeer;
import dev.minceraft.sonus.web.pion.PionRemoteTrack;
import dev.minceraft.sonus.web.pion.ipc.model.IceServer;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@NullMarked
public final class RtcHandler implements AutoCloseable, PionPeer.Callback {

    private static final Logger LOGGER = LoggerFactory.getLogger("WebRTC");
    private static final int INPUT_BUFFER_GC_INTERVAL = 0b1111111;
    private static final int QUIET_FRAMES_THRESHOLD = 300 / SonusConstants.FRAMES_INTERVAL;
    private static final double QUIET_THRESHOLD_RMS_AMPLITUDE = 0e-5d;

    private final RtcManager manager;
    private final WebSocketConnection signalConnection;
    private final PionPeer peer;

    // audio output handling
    private final AudioMixer mixer = new AudioMixer();
    private @MonotonicNonNull ScheduledFuture<?> ticker = null;
    private @MonotonicNonNull PionLocalTrack outputTrack;

    // audio input handling
    private OpusNativesLoader.@Nullable Decoder opusDecoder;
    private final ByteBuf inputBuffer = PooledByteBufAllocator.DEFAULT.buffer(SonusConstants.FRAME_SIZE * 2);
    private int inputBufferGc = INPUT_BUFFER_GC_INTERVAL;
    private long sequenceNumber = 0L;
    private int quietBuffer = QUIET_FRAMES_THRESHOLD + 1;

    public RtcHandler(RtcManager manager, WebSocketConnection signalConnection) {
        this.manager = manager;
        this.signalConnection = signalConnection;

        // configure pion
        WebConfig config = manager.getConfig();
        List<IceServer> servers = config.iceServers.stream()
                .map(WebConfig.IceServerConfig::create).toList();
        String streamId = signalConnection.getPlayer().getUniqueId().toString().substring(0, 8);
        this.peer = manager.getPion().allocatePeer(servers, config.bundlePolicy, streamId, this);
    }

    public void disconnect(String reason) {
        LOGGER.info("Disconnected {} because: {}", this.peer, reason);
        this.manager.removePeer(this.signalConnection.getPlayer().getUniqueId());
    }

    @Override
    public void onIceCandidate(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex) {
        this.signalConnection.sendPacket(new RtcIceCandidatePacket(candidate, sdpMid, sdpMLineIndex));
    }

    @Override
    public void onAnswerCreated(String sdp) {
        this.signalConnection.sendPacket(new RtcOfferPacket(RtcOfferPacket.Type.ANSWER, sdp));
    }

    @Override
    public void onIceConnectionStateChange(IceConnectionState state) {
        LOGGER.info("Received ice connection state change for {}: {}", this.peer, state);
    }

    @Override
    public void onConnectionStateChange(PeerConnectionState state) {
        LOGGER.info("Received connection state change for {}: {}", this.peer, state);
    }

    @Override
    public PionRemoteTrack.Callback onRemoteAudioTrack(PionRemoteTrack track) {
        if (this.opusDecoder != null) {
            this.opusDecoder.close();
            this.opusDecoder = null;
        }
        int sampleRate = track.getSampleRate();
        short channels = track.getChannels();
        OpusNativesLoader.Decoder opusDecoder = this.manager.getOpusLoader().new Decoder(sampleRate, channels);
        this.opusDecoder = opusDecoder;

        return new PionRemoteTrack.Callback() {
            private long lastDurationNanos = -1;
            private byte @Nullable [] lastDataArray;

            @Override
            public void onData(ByteBuf data, long durationNanos) {
                // dynamically adjust frame size depending on frame duration
                if (this.lastDurationNanos != durationNanos) {
                    int frameInterval = (int) (durationNanos / 1_000_000L);
                    opusDecoder.setFrameSize(sampleRate * frameInterval / 1000);
                    this.lastDurationNanos = durationNanos;
                }
                // re-use byte array from last frame if possible
                int dataLen = data.readableBytes();
                byte[] dataArr = this.lastDataArray;
                if (dataArr == null || dataArr.length != dataLen) {
                    this.lastDataArray = dataArr = new byte[dataLen];
                }
                data.readBytes(dataArr);
                // decode opus data and handle pcm audio frame
                short[] pcmFrame = opusDecoder.decode(dataArr);
                RtcHandler.this.handleMicInput(pcmFrame);
            }
        };
    }

    @Override
    public void onLocalAudioTrack(PionLocalTrack track) {
        this.outputTrack = track;
    }

    @Override
    public void onError(Error error) {
        LOGGER.error("Received internal error for {}", this.peer, error);
        this.disconnect("internal error");
    }

    public void startTicking(ScheduledExecutorService scheduler) {
        // start push task, needs to be run with set interval
        this.ticker = scheduler.scheduleAtFixedRate(this::tickAudio,
                RtcConstants.FRAME_INTERVAL, RtcConstants.FRAME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void tickAudio() {
        ByteBuf frame = null;
        try {
            frame = this.mixer.tick(RtcConstants.FRAME_SIZE);
            if (frame != null) {
                this.outputTrack.sendData(frame.retain(), RtcConstants.FRAME_INTERVAL * 1_000_000L);
            }
        } catch (Throwable throwable) {
            LOGGER.error("Error while ticking audio", throwable);
        } finally {
            ReferenceCountUtil.release(frame);
        }
    }

    private void handleMicInput(short[] samples) {
        ISonusPlayer player = this.signalConnection.getPlayer();
        if (player.isMuted()) {
            this.inputBuffer.clear();
            this.signalConnection.setVoiceActive(player.getUniqueId(), false);
            return;
        }

        // append to local buffer, webrtc usually has higher FPS than what we expect
        ByteBuf inputBuf = this.inputBuffer;
        for (short sample : samples) {
            inputBuf.writeShortLE(sample);
        }
        while (inputBuf.isReadable(SonusConstants.FRAME_SIZE * Short.BYTES)) {
            // read pcm shorts from buffer whilst also calculating audio level using RMS
            double rmsAmplitude = 0d;
            short[] pcmData = new short[SonusConstants.FRAME_SIZE];
            for (int i = 0; i < SonusConstants.FRAME_SIZE; i++) {
                short s = inputBuf.readShortLE();
                pcmData[i] = s;
                double amplitude = (double) s / (double) Short.MAX_VALUE;
                rmsAmplitude += amplitude * amplitude;
            }
            // check whether this is loud enough or not
            if (rmsAmplitude / (double) SonusConstants.FRAME_SIZE > QUIET_THRESHOLD_RMS_AMPLITUDE * QUIET_THRESHOLD_RMS_AMPLITUDE) {
                this.quietBuffer = 0; // reset quiet buffer
                this.signalConnection.setVoiceActive(player.getUniqueId(), true);
                SonusAudio.Pcm audio = new SonusAudio.Pcm(pcmData, this.sequenceNumber++);
                player.handleAudioInput(audio);
            } else if (this.quietBuffer == QUIET_FRAMES_THRESHOLD) {
                this.quietBuffer++;
                // mark end of input
                this.signalConnection.setVoiceActive(player.getUniqueId(), false);
                player.handleAudioInputEnd(this.sequenceNumber);
                this.sequenceNumber = 0L;
            } else if (this.quietBuffer < QUIET_FRAMES_THRESHOLD) {
                // wait a bit before marking as silent
                this.quietBuffer++;
            }

            // periodically clean buffer
            if (this.inputBufferGc-- <= 0) {
                inputBuf.discardSomeReadBytes();
                this.inputBufferGc = INPUT_BUFFER_GC_INTERVAL;
            }
        }
    }

    public void queueAudio(UUID channelId, short[] leftAudio, short[] rightAudio, float volume) {
        if (this.isConnected()) {
            this.mixer.handle(channelId, leftAudio, rightAudio, volume);
        }
    }

    public void handleRemoteIce(String candidate, @Nullable String sdpMid, @Nullable Short sdpMLineIndex) {
        LOGGER.info("Accepting ICE candidate from {}", this.signalConnection);
        this.peer.addIceCandidate(candidate, sdpMid, sdpMLineIndex);
    }

    public void handleRemoteOffer(String sdp) {
        LOGGER.info("Handling offer from {}", this.signalConnection);
        this.peer.handleOffer(sdp);
    }

    public WebSocketConnection getSignalConnection() {
        return this.signalConnection;
    }

    public boolean isConnected() {
        return this.peer.getConnectionState() == PeerConnectionState.CONNECTED;
    }

    public boolean isIceConnected() {
        return this.peer.getIceConnectionState() == IceConnectionState.CONNECTED;
    }

    @Override
    public void close() {
        this.peer.close();
        this.mixer.close();
        this.inputBuffer.release();
        if (this.ticker != null) {
            this.ticker.cancel(false);
            this.ticker = null;
        }
        if (this.opusDecoder != null) {
            this.opusDecoder.close();
            this.opusDecoder = null;
        }
    }
}
