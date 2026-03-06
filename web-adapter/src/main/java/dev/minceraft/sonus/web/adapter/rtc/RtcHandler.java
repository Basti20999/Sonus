package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (5:02 PM 02.03.2026)

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.adapter.util.AudioMixer;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcIceCandidatePacket;
import dev.minceraft.sonus.web.protocol.packets.commonbound.RtcOfferPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.PadDirection;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.SDPMessage;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.freedesktop.gstreamer.elements.DecodeBin;
import org.freedesktop.gstreamer.webrtc.WebRTCBin;
import org.freedesktop.gstreamer.webrtc.WebRTCPeerConnectionState;
import org.freedesktop.gstreamer.webrtc.WebRTCSDPType;
import org.freedesktop.gstreamer.webrtc.WebRTCSessionDescription;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@NullMarked
public final class RtcHandler implements AutoCloseable {

    private static final Caps RTC_IN_DECODED_CAPS = new Caps("audio/x-raw,format=S16LE,rate=" + SonusConstants.SAMPLE_RATE + ",channels=1");
    private static final Caps RTC_OUT_CAPS = new Caps("audio/x-raw,format=S16LE,rate=" + RtcConstants.SAMPLE_RATE + ",channels=2,layout=interleaved");
    private static final Caps RTC_OUT_ENCODED_CAPS = new Caps("application/x-rtp,media=audio,encoding-name=OPUS,payload=96");

    private static final String ELEMENT_RX_PREFIX = "rx_";
    private static final String ELEMENT_TX_PREFIX = "tx_";

    private static final Logger LOGGER = LoggerFactory.getLogger("WebRTC");
    private static final int INPUT_BUFFER_GC_INTERVAL = 0b1111111;
    private static final int QUIET_FRAMES_THRESHOLD = 300 / SonusConstants.FRAMES_INTERVAL;
    private static final double QUIET_THRESHOLD_RMS_AMPLITUDE = 0e-5d;

    private final RtcManager manager;
    private final WebSocketConnection signalConnection;
    private final Pipeline pipe = new Pipeline();
    private final WebRTCBin rtcBin = createWebRtcBin(this.pipe);

    // audio output handling
    private final AudioMixer mixer = new AudioMixer();
    private @MonotonicNonNull ScheduledFuture<?> ticker = null;
    private @MonotonicNonNull AppSrc outputSource;
    private volatile boolean push = false;

    // audio input handling
    private final ByteBuf inputBuffer = PooledByteBufAllocator.DEFAULT.buffer(SonusConstants.FRAME_SIZE * 2);
    private int inputBufferGc = INPUT_BUFFER_GC_INTERVAL;
    private long sequenceNumber = 0L;
    private int quietBuffer = QUIET_FRAMES_THRESHOLD + 1;

    public RtcHandler(RtcManager manager, WebSocketConnection signalConnection) {
        this.manager = manager;
        this.signalConnection = signalConnection;
        this.pipe.setState(State.PAUSED);
    }

    private static WebRTCBin createWebRtcBin(Pipeline pipe) {
        WebRTCBin ret = new WebRTCBin("webrtcbin");
        ret.set("bundle-policy", 3L); // max-bundle (https://gstreamer.freedesktop.org/documentation/webrtclib/webrtc_fwd.html?gi-language=c#GstWebRTCBundlePolicy)
        pipe.add(ret);
        return ret;
    }

    public void disconnect(String reason) {
        LOGGER.info("Disconnected {}/{} because: {}", this.pipe.getName(), this.rtcBin.getName(), reason);
        this.manager.removePeer(this.signalConnection.getPlayer().getUniqueId());
    }

    public void initialize(ScheduledExecutorService scheduler) {
        this.setupPipeLogging(this.pipe);

        // register handlers
        this.rtcBin.connect((WebRTCBin.ON_ICE_CANDIDATE) (sdpMLineIndex, candidate) ->
                this.signalConnection.sendPacket(new RtcIceCandidatePacket(candidate, null, sdpMLineIndex)));

        this.setupMicTrack();
        this.setupOutputTrack(scheduler);

        // start!
        this.pipe.play();
    }

    private void setupMicTrack() {
        this.rtcBin.connect((Element.PAD_ADDED) (eleme, track) -> {
            LOGGER.info("Receiving stream! Element: {} Pad: {}", eleme, track);
            if (track.getDirection() != PadDirection.SRC) {
                return;
            }
            // remove previous rx elements
            Element[] rxElements = this.pipe.getElements().stream()
                    .filter(element -> element.getName().startsWith(ELEMENT_RX_PREFIX))
                    .toArray(Element[]::new);
            if (rxElements.length > 0) {
                this.pipe.removeMany(rxElements);
                for (Element rxElement : rxElements) {
                    rxElement.close();
                }
            }

            // decode incoming stream
            DecodeBin decodeBin = new DecodeBin(ELEMENT_RX_PREFIX + "decodebin");
            decodeBin.connect((Element.PAD_ADDED) this::handleMicTrack);

            // start decoding
            this.pipe.add(decodeBin);
            decodeBin.syncStateWithParent();
            track.link(decodeBin.getStaticPad("sink"));

            this.pipe.debugToDotFile(EnumSet.allOf(Bin.DebugGraphDetails.class), "fucku");
        });
    }

    private void handleMicTrack(Element ignoredElem, Pad track) {
        if (!track.hasCurrentCaps()) {
            this.disconnect("unexpected pad added: " + track);
            return;
        }
        Caps caps = track.getCurrentCaps();
        LOGGER.info("Received stream with caps {}", caps);
        if (!caps.isAlwaysCompatible(Caps.fromString("audio/x-raw"))) {
            this.disconnect("caps are not compatible with audio");
            return;
        }

        // create rx elements
        Element q = ElementFactory.make("queue", ELEMENT_RX_PREFIX + "audioqueue");
        Element conv = ElementFactory.make("audioconvert", ELEMENT_RX_PREFIX + "audioconvert");
        Element resample = ElementFactory.make("audioresample", ELEMENT_RX_PREFIX + "audioresample");
        Element capsFilter = ElementFactory.make("capsfilter", ELEMENT_RX_PREFIX + "cfilter");
        capsFilter.setCaps(RTC_IN_DECODED_CAPS);
        AppSink sink = new AppSink(ELEMENT_RX_PREFIX + "micsink");

        // add nodes
        this.pipe.addMany(q, conv, resample, capsFilter, sink);
        q.syncStateWithParent();
        conv.syncStateWithParent();
        resample.syncStateWithParent();
        capsFilter.syncStateWithParent();
        sink.syncStateWithParent();

        // link nodes
        track.link(q.getStaticPad("sink"));
        Element.linkMany(q, conv, resample, capsFilter, sink);

        // setup sink
        sink.set("emit-signals", true);
        sink.set("sync", false);
        sink.connect((AppSink.NEW_SAMPLE) elem -> {
            Buffer buffer = elem.pullSample().getBuffer();
            ByteBuffer nioBuf = buffer.map(false);
            try {
                this.handleMicInput(nioBuf);
            } finally {
                buffer.unmap();
            }
            return FlowReturn.OK;
        });
    }

    private void setupOutputTrack(ScheduledExecutorService scheduler) {
        // 2 is sendonly (https://gstreamer.freedesktop.org/documentation/webrtclib/webrtc_fwd.html?gi-language=c#GstWebRTCRTPTransceiverDirection)
        this.rtcBin.emit("add-transceiver", 2L, RTC_OUT_ENCODED_CAPS);

        // create audio output pipeline elements
        AppSrc outSrc = new AppSrc(ELEMENT_TX_PREFIX + "src");
        outSrc.set("format", 3L); // time (https://gstreamer.freedesktop.org/documentation/gstreamer/gstformat.html?gi-language=c#GstFormat)
        outSrc.set("is-live", true);
        outSrc.set("do-timestamp", true);
        outSrc.set("emit-signals", true);
        outSrc.setCaps(RTC_OUT_CAPS);
        Element srcFilter = ElementFactory.make("capsfilter", ELEMENT_TX_PREFIX + "srcfilter");
        srcFilter.setCaps(RTC_OUT_CAPS);
        Element conv = ElementFactory.make("audioconvert", ELEMENT_TX_PREFIX + "audioconvert");
        Element resample = ElementFactory.make("audioresample", ELEMENT_TX_PREFIX + "audioresample");
        Element encoder = ElementFactory.make("opusenc", ELEMENT_TX_PREFIX + "opusencoder");
        Element realtime = ElementFactory.make("rtpopuspay", ELEMENT_TX_PREFIX + "rtpopuspay");
        Element dstFilter = ElementFactory.make("capsfilter", ELEMENT_TX_PREFIX + "dstfilter");
        dstFilter.setCaps(RTC_OUT_ENCODED_CAPS);
        Element queue = ElementFactory.make("queue", ELEMENT_TX_PREFIX + "queue");

        // add to pipeline
        this.pipe.addMany(outSrc, srcFilter, conv, resample, encoder, realtime, dstFilter, queue);
        outSrc.syncStateWithParent();
        srcFilter.syncStateWithParent();
        conv.syncStateWithParent();
        resample.syncStateWithParent();
        encoder.syncStateWithParent();
        realtime.syncStateWithParent();
        dstFilter.syncStateWithParent();
        queue.syncStateWithParent();

        this.outputSource = outSrc;
        outSrc.connect((AppSrc.ENOUGH_DATA) elem -> this.push = false);
        outSrc.connect((AppSrc.NEED_DATA) (elem, size) -> this.push = true);

        // start push task, needs to be run with set interval
        this.ticker = scheduler.scheduleAtFixedRate(this::tickAudio,
                RtcConstants.FRAME_INTERVAL, RtcConstants.FRAME_INTERVAL, TimeUnit.MILLISECONDS);

        // link
        Element.linkMany(outSrc, srcFilter, conv, resample, encoder, realtime, dstFilter, queue, this.rtcBin);
    }

    private void tickAudio() {
        try {
            this.tickAudio0();
        } catch (Throwable throwable) {
            LOGGER.error("Error while ticking audio", throwable);
        }
    }

    private void tickAudio0() {
        // *2*2 because 16-bit and stereo
        Buffer buf = new Buffer(RtcConstants.FRAME_SIZE << 2);
        this.mixer.tick(buf.map(true));
        FlowReturn ret = null;
        if (this.push) {
            buf.unmap();
            ret = this.outputSource.pushBuffer(buf);
        } else {
            buf.close();
        }
    }

    private void handleMicInput(ByteBuffer data) {
        ISonusPlayer player = this.signalConnection.getPlayer();
        if (player.isMuted()) {
            this.inputBuffer.clear();
            this.signalConnection.setVoiceActive(player.getUniqueId(), false);
            return;
        }

        // append to local buffer, webrtc usually has higher FPS than what we expect
        ByteBuf inputBuf = this.inputBuffer;
        inputBuf.writeBytes(data);
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

    private void setupPipeLogging(Pipeline pipe) {
        Bus bus = pipe.getBus();
        bus.connect((Bus.EOS) source ->
                this.disconnect("Reached end of stream: " + source.toString()));

        bus.connect((Bus.ERROR) (source, code, message) ->
                this.disconnect("Error from source: " + source + ", with code: " + code + ", and message: " + message));

        bus.connect((source, old, current, pending) -> {
            if (source instanceof Pipeline) {
                LOGGER.info("Pipe {} state changed from {} to {}", this.pipe, old, current);
            }
        });
    }

    public void handleRemoteIce(@Nullable String sdp, @Nullable Integer sdpMLineIndex) {
        this.rtcBin.addIceCandidate(sdpMLineIndex == null ? -1 : sdpMLineIndex, sdp);
    }

    public void handleRemoteOffer(WebRTCSDPType type, @Nullable String sdp) {
        // configure remote description
        WebRTCSessionDescription remoteSessionDesc;
        if (sdp != null) {
            SDPMessage sdpMsg = new SDPMessage();
            sdpMsg.parseBuffer(sdp);
            remoteSessionDesc = new WebRTCSessionDescription(type, sdpMsg);
        } else {
            remoteSessionDesc = null;
        }
        this.rtcBin.setRemoteDescription(remoteSessionDesc);

        // construct answer
        this.rtcBin.createAnswer(answer -> {
            this.rtcBin.setLocalDescription(answer);
            // send to web app
            String answerSdp = answer.getSDPMessage().toString();
            this.signalConnection.sendPacket(new RtcOfferPacket("answer", answerSdp));
        });
    }

    public void addTurnServer(String uri, @Nullable String user, @Nullable String auth) {
        try {
            URI parsedUri = new URI(uri);

            // gstreamer only supports simple stun servers
            if ("stun".equals(parsedUri.getScheme())) {
                this.rtcBin.setStunServer(uri);
                return;
            }

            if (user != null) {
                String encodedUser = URLEncoder.encode(user, StandardCharsets.UTF_8);
                String encodedAuth = auth != null ? URLEncoder.encode(auth, StandardCharsets.UTF_8) : null;
                String userInfo = encodedUser + (auth != null ? ":" + encodedAuth : "");
                // inject user info string into uri
                URI turnUri = new URI(parsedUri.getScheme(), userInfo,
                        parsedUri.getHost(), parsedUri.getPort(), parsedUri.getRawPath(),
                        parsedUri.getRawQuery(), parsedUri.getRawFragment());
                uri = turnUri.toString();
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Invalid turn uri: " + uri, exception);
        }

        this.rtcBin.emit("add-turn-server", uri);
    }

    public WebSocketConnection getSignalConnection() {
        return this.signalConnection;
    }

    public boolean isConnected() {
        return this.rtcBin.getConnectionState() == WebRTCPeerConnectionState.CONNECTED;
    }

    @Override
    public void close() {
        this.pipe.setState(State.NULL);
        this.pipe.close();
        this.mixer.close();
        this.inputBuffer.release();
        if (this.ticker != null) {
            this.ticker.cancel(false);
            this.ticker = null;
        }
    }
}
