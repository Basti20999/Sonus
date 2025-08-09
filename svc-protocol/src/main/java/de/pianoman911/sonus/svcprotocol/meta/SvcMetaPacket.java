package de.pianoman911.sonus.svcprotocol.meta;


import de.pianoman911.sonus.svcprotocol.AbstractSvcPacket;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.IJsonCodec;
import net.kyori.adventure.key.Key;


public abstract class SvcMetaPacket<T extends SvcMetaPacket<?>> extends AbstractSvcPacket implements IJsonCodec<ISonusPlayer, IMetaSvcHandler> {

    protected final Key pluginMessageChannel;

    protected SvcMetaPacket(Key pluginMessageChannel) {
        this.pluginMessageChannel = pluginMessageChannel;
    }

    public abstract void handle(ISonusPlayer player, IMetaSvcHandler handler);

    public Key getPluginMessageChannel() {
        return this.pluginMessageChannel;
    }
}
