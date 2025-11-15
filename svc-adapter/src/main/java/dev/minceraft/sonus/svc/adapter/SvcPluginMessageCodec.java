package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import dev.minceraft.sonus.common.protocol.tcp.holder.PmDataHolderBuf;
import dev.minceraft.sonus.svc.adapter.connection.SvcConnection;
import dev.minceraft.sonus.svc.protocol.meta.RequestSecretSvcPacket;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.registries.SvcMetaPacketRegistry;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import dev.minceraft.sonus.svc.protocol.version.VersionManager;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.minceraft.sonus.common.SonusConstants.PERMISSION_CONNECT_SVC;

@NullMarked
public class SvcPluginMessageCodec extends AbstractPluginMessageCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SvcProtocolAdapter protocolAdapter;

    public SvcPluginMessageCodec(SvcProtocolAdapter protocolAdapter) {
        super(SvcPluginChannels.getChannels());
        this.protocolAdapter = protocolAdapter;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, MessageSource source, ISonusPlayer player) {
        if (source == MessageSource.SERVER) {
            LOGGER.warn("{} was sent a packet from the server", player.getName());
            return;
        }

        SvcSessionManager sessionManager = this.protocolAdapter.getAdapter().getSessions();
        SvcConnection connection = sessionManager.getConnection(player.getUniqueId());

        PmDataHolderBuf data = PmDataHolderBuf.newInstance(packet, channel);
        SvcMetaPacket<? extends SvcMetaPacket<?>> metaPacket;
        try {
            int version;
            if (connection == null) {
                version = VersionManager.OLDEST_VERSION;
            } else {
                version = connection.getVersion();
            }

            metaPacket = SvcMetaPacketRegistry.BUF_REGISTRY.read(data, new SvcMetaPacketRegistry.SvcMetaContext(version));
        } finally {
            data.recycle();
        }
        if (metaPacket == null) {
            return;
        }

        if (connection == null) {
            if (metaPacket instanceof RequestSecretSvcPacket secret) { // Initial connection
                connection = this.initConnection(secret, player);
                sessionManager.addConnection(connection);
            } else {
                // No connection found for the player, handle accordingly
                return;
            }
        } else if (metaPacket instanceof RequestSecretSvcPacket) {
            LOGGER.warn("Received RequestSecretSvcPacket for player {} but connection already exists. Ignoring.", player.getUniqueId());
            return;
        }
        if (connection == null) { // Incompatible version or failed to initialize connection
            return;
        }
        metaPacket.handle(connection.getMetaHandler());
    }

    @Nullable
    private SvcConnection initConnection(RequestSecretSvcPacket packet, ISonusPlayer player) {
        if (!VersionManager.SUPPORTED_VERSIONS.contains(packet.getCompatibilityVersion())) {
            return null; // Incompatible version
        }
        if (!player.hasPermission(PERMISSION_CONNECT_SVC, TriState.TRUE)) {
            return null;
        }
        return new SvcConnection(this.protocolAdapter, player);
    }
}
