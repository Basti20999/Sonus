package de.pianoman911.sonus.svcprotocol;

import de.pianoman911.sonus.svcprotocol.meta.IMetaSvcHandler;
import de.pianoman911.sonus.svcprotocol.meta.SvcMetaPacket;
import de.pianoman911.sonus.svcprotocol.registries.SvcMetaPacketRegistry;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
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
