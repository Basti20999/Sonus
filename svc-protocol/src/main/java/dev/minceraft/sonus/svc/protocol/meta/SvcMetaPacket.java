package dev.minceraft.sonus.svc.protocol.meta;


import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.codec.IJsonCodec;
import dev.minceraft.sonus.common.version.Versioned;
import dev.minceraft.sonus.svc.protocol.AbstractSvcPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;


@NullMarked
public abstract class SvcMetaPacket extends AbstractSvcPacket<IMetaSvcHandler> implements IJsonCodec<IMetaSvcHandler> {

    protected final Versioned<Key> pluginMessageChannel;

    protected SvcMetaPacket(Key pluginMessageChannel) {
        this.pluginMessageChannel = Versioned.createSingle(pluginMessageChannel);
    }

    protected SvcMetaPacket(Versioned<Key> pluginMessageChannels) {
        this.pluginMessageChannel = pluginMessageChannels;
    }

    public Versioned<Key> getPluginMessageChannel() {
        return this.pluginMessageChannel;
    }

    @Override
    public String toString() {
        JsonObject re = new JsonObject();
        this.encode(re);
        return this.getClass().getSimpleName() + re;
    }
}
