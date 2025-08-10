package dev.minceraft.sonus.svc.protocol;

import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.registries.SvcMetaPacketRegistry;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;

public class SvcPluginMessageCodec extends AbstractPluginMessageCodec {

    private final IMetaSvcHandler handler;

    public SvcPluginMessageCodec(IMetaSvcHandler handler) {
        super(SvcPluginChannels.getPackets());
        this.handler = handler;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, ISonusPlayer player) {
        SvcMetaPacket<? extends SvcMetaPacket<?>> metaPacket = SvcMetaPacketRegistry.read(channel, packet);
        metaPacket.handle(player, this.handler);
    }
}
