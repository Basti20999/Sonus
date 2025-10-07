package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import dev.minceraft.sonus.plasmo.adapter.connection.PlasmoConnection;
import dev.minceraft.sonus.plasmo.protocol.PlasmoPmChannels;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPacketRegistry;
import dev.minceraft.sonus.plasmo.protocol.tcp.TcpPlasmoPacket;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlasmoPluginMessageCodec extends AbstractPluginMessageCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");
    private final PlasmoAdapter adapter;

    public PlasmoPluginMessageCodec(PlasmoAdapter adapter) {
        super(PlasmoPmChannels.CHANNELS);
        this.adapter = adapter;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, MessageSource source, ISonusPlayer player) {
        if (source == MessageSource.SERVER) {
            LOGGER.warn("{}({}) was sent a packet from the server", player.getName(), player.getUniqueId());
            return;
        }
        PlasmoConnection connection = this.adapter.getSessionManager().getConnectionByUniqueId(player.getUniqueId());
        if (connection == null) {
            LOGGER.warn("{}({}) tried to connect without initial invite", player.getName(), player.getUniqueId());
            return;
        }

        TcpPlasmoPacket<?> read = TcpPacketRegistry.REGISTRY.read(packet);
        if (read == null) {
            LOGGER.warn("Failed to read packet from {}({})", player.getName(), player.getUniqueId());
            return;
        }
        read.handle(connection.getMetaHandler());
    }
}
