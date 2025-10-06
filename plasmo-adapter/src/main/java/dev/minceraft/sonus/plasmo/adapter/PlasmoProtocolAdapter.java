package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.common.protocol.udp.UdpBasedContext;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoCipherCodec;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoPacketCodec;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoPlayerMarkerCodec;
import dev.minceraft.sonus.plasmo.adapter.pipeline.PlasmoUdpContext;
import dev.minceraft.sonus.plasmo.protocol.PlasmoUdpMagicCodec;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlasmoProtocolAdapter implements VoiceProtocolAdapter {

    private final PlasmoUdpMagicCodec plasmoCodec = new PlasmoUdpMagicCodec(this);

    public PlasmoProtocolAdapter(PlasmoAdapter adapter) {
        ISonusService service = adapter.getService();
        IUdpServer udpServer = service.getUdpServer();

        udpServer.registerCodec(this.plasmoCodec);
        udpServer.registerHandler("plasmo-codec", new PlasmoPacketCodec(this.plasmoCodec));
        udpServer.registerHandler("plasmo-player-marker", new PlasmoPlayerMarkerCodec(this.plasmoCodec, adapter));
        udpServer.registerHandler("plasmo-cipher", new PlasmoCipherCodec(this.plasmoCodec));


        service.getPluginMessenger().registerCodec(new PlasmoPluginMessageCodec());
    }

    @Override
    public byte getMagicByte() {
        return this.plasmoCodec.getMagicByte();
    }

    @Override
    public UdpBasedContext<?> newPipelineContext() {
        return PlasmoUdpContext.newInstance();
    }

    public PlasmoUdpMagicCodec getPlasmoCodec() {
        return this.plasmoCodec;
    }
}
