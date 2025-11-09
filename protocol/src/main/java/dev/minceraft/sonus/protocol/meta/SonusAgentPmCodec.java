package dev.minceraft.sonus.protocol.meta;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class SonusAgentPmCodec extends AbstractPluginMessageCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");
    private final IAgentManager agentManager;

    public SonusAgentPmCodec(Set<Key> supportedChannels, IAgentManager agentManager) {
        super(supportedChannels);
        this.agentManager = agentManager;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, MessageSource source, ISonusPlayer player) {
        if (source == MessageSource.PLAYER) {
            LOGGER.warn("{} tried to send data on the Sonus Internal channel. ", player.getName());
            return;
        }
        IMetaMessage msg = MetaRegistry.REGISTRY.read(packet);
        if (msg == null) {
            return;
        }
        msg.handle(this.agentManager.getAgentListener(player.getServerId()));
    }
}
