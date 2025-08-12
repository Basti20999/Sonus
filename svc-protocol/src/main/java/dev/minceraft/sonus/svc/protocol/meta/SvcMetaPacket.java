package dev.minceraft.sonus.svc.protocol.meta;


import dev.minceraft.sonus.common.protocol.codec.IJsonCodec;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;


@NullMarked
public abstract class SvcMetaPacket<T extends SvcMetaPacket<?>> extends AbstractSvcPacket<IMetaSvcHandler> implements IJsonCodec<IMetaSvcHandler> {

    protected final Key pluginMessageChannel;

    protected SvcMetaPacket(Key pluginMessageChannel) {
        this.pluginMessageChannel = pluginMessageChannel;
    }

    public Key getPluginMessageChannel() {
        return this.pluginMessageChannel;
    }
}
