package dev.minceraft.sonus.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcCipherCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcFrameCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcHandler;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPacketCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcPlayerMarkerCodec;
import dev.minceraft.sonus.svc.adapter.pipeline.SvcUdpContext;
import dev.minceraft.sonus.svc.protocol.SvcUdpMagicCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcProtocolAdapter implements VoiceProtocolAdapter {

    private final SvcAdapter adapter;
    private final SvcUdpMagicCodec svcCodec = new SvcUdpMagicCodec(this);

    public SvcProtocolAdapter(SvcAdapter adapter) {
        this.adapter = adapter;
        ISonusService service = adapter.getService();
        IUdpServer udpServer = service.getUdpServer();

        udpServer.registerCodec(this.svcCodec);
        udpServer.registerHandler("svc-player-marker", new SvcPlayerMarkerCodec(this));
        udpServer.registerHandler("svc-frame", SvcFrameCodec.INSTANCE);
        udpServer.registerHandler("svc-cipher", SvcCipherCodec.INSTANCE);
        udpServer.registerHandler("svc-codec", SvcPacketCodec.INSTANCE);
        udpServer.registerHandler("svc-handler", new SvcHandler(service));

        service.getPluginMessenger().registerCodec(new SvcPluginMessageCodec(this));
    }

    @Override
    public byte getMagicByte() {
        return SvcUdpMagicCodec.MAGIC_BYTE;
    }

    @Override
    public UdpBasedContext<?> newPipelineContext() {
        return SvcUdpContext.newInstance();
    }

    public SvcAdapter getAdapter() {
        return this.adapter;
    }

    public SvcUdpMagicCodec getSvcCodec() {
        return this.svcCodec;
    }
}
