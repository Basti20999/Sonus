package dev.minceraft.sonus.web.protocol.packets.servicebound;
// Created by booky10 in Sonus (20:33 28.11.2025)

import dev.minceraft.sonus.common.SonusConstants;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.web.protocol.WsPacketContext;
import dev.minceraft.sonus.web.protocol.packets.IWebSocketHandler;
import dev.minceraft.sonus.web.protocol.packets.WebsocketPacket;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class InputSoundPacket extends WebsocketPacket {

    private static final int MAX_OPUS_BITRATE = 64_000; // according to https://wiki.xiph.org/Opus_Recommended_Settings#Bandwidth_Transition_Thresholds
    private static final int MAX_OPUS_BYTES_PER_FRAME = MAX_OPUS_BITRATE / Byte.SIZE / SonusConstants.FRAMES_PER_SECOND;

    private SonusAudio.@MonotonicNonNull Opus audio;
    private boolean noiseReduction;

    public InputSoundPacket(SonusAudio.Opus audio, boolean noiseReduction) {
        this.audio = audio;
        this.noiseReduction = noiseReduction;
    }

    public InputSoundPacket() {
    }

    @Override
    public void encode(ByteBuf buf, WsPacketContext context) {
        DataTypeUtil.VAR_INT.writeByteArray(buf, this.audio.opus());
        buf.writeBoolean(this.noiseReduction);
    }

    @Override
    public void decode(ByteBuf buf, WsPacketContext context) {
        byte[] opusBytes = DataTypeUtil.VAR_INT.readByteArray(buf, MAX_OPUS_BYTES_PER_FRAME);
        this.audio = new SonusAudio.Opus(opusBytes, 0L);
        this.noiseReduction = buf.readBoolean();
    }

    @Override
    public void handle(IWebSocketHandler handler) {
        handler.handleInputSound(this);
    }

    public SonusAudio.Opus getAudio() {
        return this.audio;
    }

    public void setAudio(SonusAudio.Opus audio) {
        this.audio = audio;
    }

    public boolean isNoiseReduction() {
        return this.noiseReduction;
    }

    public void setNoiseReduction(boolean noiseReduction) {
        this.noiseReduction = noiseReduction;
    }
}
