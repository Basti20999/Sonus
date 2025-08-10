package dev.minceraft.sonus.svc.protocol;


import dev.minceraft.sonus.svc.protocol.voice.SvcVoicePacket;
import dev.minceraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;
import io.leangen.geantyref.TypeToken;

public class SvcUdpMagicCodec extends AbstractMagicUdpCodec<SvcVoicePacket<?>> {

    public static final SvcUdpMagicCodec INSTANCE = new SvcUdpMagicCodec();

    public static final byte MAGIC_BYTE = (byte) 0b11111111;

    public SvcUdpMagicCodec() {
        super(MAGIC_BYTE, new TypeToken<SvcVoicePacket<?>>() {});
    }
}
