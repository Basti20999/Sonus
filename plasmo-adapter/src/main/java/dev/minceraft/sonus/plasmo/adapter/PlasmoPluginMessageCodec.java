package dev.minceraft.sonus.plasmo.adapter;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import dev.minceraft.sonus.plasmo.protocol.PlasmoPmChannels;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlasmoPluginMessageCodec extends AbstractPluginMessageCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    public PlasmoPluginMessageCodec() {
        super(PlasmoPmChannels.CHANNELS);
    }

    @Override
    public void handle(ByteBuf packet, Key channel, MessageSource source, ISonusPlayer player) {
        if (source == MessageSource.SERVER) {
            LOGGER.warn("{} was sent a packet from the server", player.getName());
            return;
        }
    }
}
