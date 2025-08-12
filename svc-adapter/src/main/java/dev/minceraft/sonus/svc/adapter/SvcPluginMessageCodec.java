package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.registries.SvcMetaPacketRegistry;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SvcPluginMessageCodec extends AbstractPluginMessageCodec {

    private final SvcProtocolAdapter adapter;

    public SvcPluginMessageCodec(SvcProtocolAdapter adapter) {
        super(SvcPluginChannels.getPackets());
        this.adapter = adapter;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, ISonusPlayer player) {
        PmDataHolderBuf data = PmDataHolderBuf.newInstance(packet, channel);
        SvcMetaPacket<? extends SvcMetaPacket<?>> metaPacket;
        try {
            metaPacket = SvcMetaPacketRegistry.BUF_REGISTRY.read(data);
        } finally {
            data.recycle();
        }
        if (metaPacket == null) {
            return;
        }

        // TODO: Init connection via request secret packet -> new connection
        SvcConnection connection = this.adapter.getSessionManager().getConnection(player.getUniqueId());
        if (connection == null) {
            // No connection found for the player, handle accordingly
            return;
        }
        metaPacket.handle(connection.getMetaHandler());
    }
}
