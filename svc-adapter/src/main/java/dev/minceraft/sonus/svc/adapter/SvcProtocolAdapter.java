package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcCipherFrontendCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcFrameCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcHandler;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPacketCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPlayerMarkerCodec;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcProtocolAdapter implements VoiceProtocolAdapter {

    private final SvcCipherFrontendCodec cipher = new SvcCipherFrontendCodec();

    public SvcProtocolAdapter(ISonusService service) {
        IUdpServer udpServer = service.getUdpServer();

        udpServer.registerCodec(SvcUdpMagicCodec.INSTANCE);
        udpServer.registerHandler("svc-player-marker", SvcPlayerMarkerCodec.INSTANCE);
        udpServer.registerHandler("svc-frame", SvcFrameCodec.INSTANCE);
        udpServer.registerHandler("svc-cipher", this.cipher);
        udpServer.registerHandler("svc-codec", SvcPacketCodec.INSTANCE);
        udpServer.registerHandler("svc-handler", new SvcHandler(service));
    }

    @Override
    public byte getMagicByte() {
        return SvcUdpMagicCodec.MAGIC_BYTE;
    }
}
