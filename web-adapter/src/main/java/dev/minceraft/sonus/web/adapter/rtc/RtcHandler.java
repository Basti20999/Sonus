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
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Bus;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.FlowReturn;
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

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@NullMarked
public final class RtcHandler implements AutoCloseable {

    private static final Caps RTC_IN_DECODED_CAPS = new Caps("audio/x-raw,format=S16LE,rate=" + SonusConstants.SAMPLE_RATE + ",channels=1");
    private static final Caps RTC_OUT_CAPS = new Caps("audio/x-raw,format=S16LE,rate=" + RtcConstants.SAMPLE_RATE + ",channels=2,layout=interleaved");
    private static final Caps RTC_OUT_ENCODED_CAPS = new Caps("application/x-rtp,media=audio,encoding-name=OPUS,payload=96");

    private static final String GST_PIPE_DESC_TX = ""
            + "appsrc name=output0 format=time is-live=true do-timestamp=true ! " + RTC_OUT_CAPS
            + " ! audioconvert ! audioresample ! queue ! opusenc ! rtpopuspay ! queue ! " + RTC_OUT_ENCODED_CAPS
            + " ! webrtcbin. ";
    private static final String GST_PIPE_DESC_RX = ""
            + "webrtcbin name=webrtcbin bundle-policy=max-bundle stun-server=%s";
    private static final String GST_PIPE_DESC = GST_PIPE_DESC_TX + " " + GST_PIPE_DESC_RX + " ";

    private static final Logger LOGGER = LoggerFactory.getLogger("WebRTC");
    private static final int INPUT_BUFFER_GC_INTERVAL = 0b1111111;
    private static final int QUIET_FRAMES_THRESHOLD = 300 / SonusConstants.FRAMES_INTERVAL;

    private final RtcManager manager;
    private final WebSocketConnection signalConnection;
    private final Pipeline pipe = new Pipeline();
    private final WebRTCBin rtcBin = createWebRtcBin(this.pipe);

    // audio output handling
    private final AudioMixer mixer = new AudioMixer();

    // audio input handling
    private final ByteBuf inputBuffer = PooledByteBufAllocator.DEFAULT.buffer(SonusConstants.FRAME_SIZE * 2);
    private int inputBufferGc = INPUT_BUFFER_GC_INTERVAL;
    private long sequenceNumber = 0L;
    private int quietBuffer = QUIET_FRAMES_THRESHOLD + 1;

    public RtcHandler(RtcManager manager, String stun, WebSocketConnection signalConnection) {
        this.manager = manager;
        this.signalConnection = signalConnection;

        this.rtcBin.set("stun-server", stun);
        this.pipe.setState(State.PAUSED);
        this.initialize();
    }

    private static WebRTCBin createWebRtcBin(Pipeline pipe) {
        WebRTCBin ret = (WebRTCBin) ElementFactory.make("webrtcbin", "webrtcbin");
        ret.set("bundle-policy", 3L); // max-bundle (https://gstreamer.freedesktop.org/documentation/webrtclib/webrtc_fwd.html?gi-language=c#GstWebRTCBundlePolicy)
        pipe.add(ret);
        return ret;
    }

    public void disconnect(String reason) {
        LOGGER.info("Disconnected {}/{} because: {}", this.pipe.getName(), this.rtcBin.getName(), reason);
        this.manager.removePeer(this.signalConnection.getPlayer().getUniqueId());
    }

    private void initialize() {
        this.setupPipeLogging(this.pipe);

        // register handlers
        this.rtcBin.connect((WebRTCBin.ON_ICE_CANDIDATE) (sdpMLineIndex, candidate) ->
                this.signalConnection.sendPacket(new RtcIceCandidatePacket(candidate, null, sdpMLineIndex)));
        this.rtcBin.connect((Element.PAD_ADDED) (uncElement, uncPad) -> {
            LOGGER.info("Receiving stream! Element: {} Pad: {}", uncElement, uncPad);
            if (uncPad.getDirection() != PadDirection.SRC) {
                return;
            }
            DecodeBin decodeBin = new DecodeBin("rxdecodebin_" + uncPad.getName());
            decodeBin.connect((Element.PAD_ADDED) (element, pad) -> { // decoded stream
                if (!pad.hasCurrentCaps()) {
                    this.disconnect("unexpected pad added: " + pad);
                    return;
                }
                Caps caps = pad.getCurrentCaps();
                LOGGER.info("Received stream with caps {}", caps, new Throwable());
                if (!caps.isAlwaysCompatible(Caps.fromString("audio/x-raw"))) {
                    this.disconnect("caps are not compatible with audio");
                    return;
                }
                // create nodes
                Element q = ElementFactory.make("queue", "rxaudioqueue");
                Element conv = ElementFactory.make("audioconvert", "rxaudioconvert");
                Element resample = ElementFactory.make("audioresample", "rxaudioresample");
                Element capsFilter = ElementFactory.make("capsfilter", "rxcfilter");
                capsFilter.setCaps(RTC_IN_DECODED_CAPS);
                Element sink = ElementFactory.make("appsink", "rxmicsink");

                // add nodes
                this.pipe.addMany(q, conv, resample, capsFilter, sink);
                q.syncStateWithParent();
                conv.syncStateWithParent();
                resample.syncStateWithParent();
                capsFilter.syncStateWithParent();
                sink.syncStateWithParent();

                // link nodes
                pad.link(q.getStaticPad("sink"));
                q.link(conv);
                conv.link(resample);
                resample.link(capsFilter);
                capsFilter.link(sink);

                // setup sink
                sink.set("emit-signals", true);
                sink.set("sync", false);
                ((AppSink) sink).connect((AppSink.NEW_SAMPLE) elem -> {
                    Buffer buffer = elem.pullSample().getBuffer();
                    ByteBuffer nioBuf = buffer.map(false);
                    try {
                        this.handleMicInput(nioBuf);
                    } finally {
                        buffer.unmap();
                    }
                    return FlowReturn.OK;
                });
            });

            this.pipe.add(decodeBin);
            decodeBin.syncStateWithParent();
            uncPad.link(decodeBin.getStaticPad("sink"));
        });

        if (false) {
            // handle audio output
        /*
            + "appsrc name=output0 format=time is-live=true do-timestamp=true ! " + RTC_OUT_CAPS
            + " ! audioconvert ! audioresample ! queue ! opusenc ! rtpopuspay ! queue ! " + RTC_OUT_ENCODED_CAPS
            + " ! webrtcbin. ";
         */
            AppSrc outSrc = (AppSrc) ElementFactory.make("appsrc", "txsrc");
            Element conv = ElementFactory.make("audioconvert", "txaudioconvert");
            Element resample = ElementFactory.make("audioresample", "txaudioresample");
            Element queue = ElementFactory.make("queue", "txaudioqueue");
            Element encoder = ElementFactory.make("opusenc", "txopusencoder");
            Element realtime = ElementFactory.make("rtpopuspay", "txrtpopuspay");
            Element rtcQueue = ElementFactory.make("queue", "txrtcqueue");
            this.pipe.addMany(outSrc, conv, resample, queue, encoder, realtime, rtcQueue);
            outSrc.syncStateWithParent();
            conv.syncStateWithParent();
            resample.syncStateWithParent();
            queue.syncStateWithParent();
            encoder.syncStateWithParent();
            realtime.syncStateWithParent();
            rtcQueue.syncStateWithParent();

//        AppSrc outSrc = (AppSrc) this.pipe.getElementByName("output0");
//        appSrc.set("emit-signals", true);
//        appSrc.connect((AppSrc.NEED_DATA) (elem, size) -> {
//            System.out.println("NEED: " + size);
//            // *2*2 because 16-bit and stereo
//            Buffer buf = new Buffer(size == -1 ? RtcConstants.FRAME_SIZE << 2 : size);
//            this.mixer.tick(buf.map(true));
//            elem.pushBuffer(buf);
//        });

            outSrc.set("format", "time");
            outSrc.set("is-live", true);
            outSrc.set("do-timestamp", true);
            outSrc.set("block", true);
            outSrc.setCaps(RTC_OUT_CAPS);
            // start push task
            ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
            sched.scheduleAtFixedRate(() -> {
                // *2*2 because 16-bit and stereo
                Buffer buf = new Buffer(RtcConstants.FRAME_SIZE << 2);
                this.mixer.tick(buf.map(true));
                buf.unmap();
                outSrc.pushBuffer(buf);
                System.out.println("PUSHED: " + System.nanoTime() / 1_000_000d + " " + outSrc.getState());
            }, RtcConstants.FRAME_INTERVAL, RtcConstants.FRAME_INTERVAL, TimeUnit.MILLISECONDS);

            // link
            outSrc.link(conv);
            conv.link(resample);
            resample.link(queue);
            queue.link(encoder);
            encoder.link(realtime);
            realtime.link(rtcQueue);
            rtcQueue.link(this.rtcBin);
        }

        // start!
        this.pipe.play();
    }

    public void handleMicInput(ByteBuffer data) {
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
            if (rmsAmplitude / (double) SonusConstants.FRAME_SIZE > 0e-6d * 0e-6d) {
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
    }
}
