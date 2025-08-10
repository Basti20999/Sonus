package dev.minceraft.sonus.svc.protocol.meta;


import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.codec.IJsonCodec;
import net.kyori.adventure.key.Key;


public abstract class SvcMetaPacket<T extends SvcMetaPacket<?>> extends AbstractSvcPacket implements IJsonCodec<IMetaSvcHandler> {

    protected final Key pluginMessageChannel;

    protected SvcMetaPacket(Key pluginMessageChannel) {
        this.pluginMessageChannel = pluginMessageChannel;
    }

    public abstract void handle(ISonusPlayer player, IMetaSvcHandler handler);

    public Key getPluginMessageChannel() {
        return this.pluginMessageChannel;
    }
}
