package dev.minceraft.sonus.protocol.meta;

import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.IPluginMessageSource;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class SonusAgentPmCodec extends AbstractPluginMessageCodec {

    private final IAgentManager agentManager;

    public SonusAgentPmCodec(Set<Key> supportedChannels, IAgentManager agentManager) {
        super(supportedChannels);
        this.agentManager = agentManager;
    }

    @Override
    public void handle(ByteBuf packet, Key channel, IPluginMessageSource source) {
        if (!(source instanceof IPluginMessageSource.Server)) {
            return;
        }
        UUID serverId = source.getServerId();
        if (serverId == null) {
            return; // no server id set somehow
        }

        IMetaMessage msg = MetaRegistry.REGISTRY.decode(packet);
        if (msg != null) {
            msg.handle(this.agentManager.getAgentListener(serverId));
        }
    }
}
