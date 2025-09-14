package dev.minceraft.sonus.service;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.tcp.AbstractPluginMessageCodec;
import dev.minceraft.sonus.common.protocol.tcp.IPluginMessenger;
import dev.minceraft.sonus.common.protocol.tcp.MessageSource;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@NullMarked
public class SonusPluginMessenger implements IPluginMessenger {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final SonusService service;
    private final Map<Key, AbstractPluginMessageCodec> codecs = new HashMap<>();

    public SonusPluginMessenger(SonusService service) {
        this.service = service;
    }

    @Override
    public void registerCodec(AbstractPluginMessageCodec codec) {
        for (Key channel : codec.getSupportedChannels()) {
            if (this.codecs.containsKey(channel)) {
                throw new IllegalArgumentException("Codec for channel " + channel + " already registered");
            }
            this.service.getPlatform().registerPluginChannel(channel);
            this.codecs.put(channel, codec);
        }
        LOGGER.info("Registered plugin message codec: {} ({})", codec.getClass().getSimpleName(),
                codec.getSupportedChannels().stream().map(Key::asString).collect(Collectors.joining(", ")));
    }

    public boolean handleMessage(Key channel, MessageSource source, ISonusPlayer player, byte[] data) {
        AbstractPluginMessageCodec codec = this.codecs.get(channel);
        if (codec == null) {
            return false; // Ignore
        }
        codec.handle(Unpooled.wrappedBuffer(data), channel, source, player);
        return true;
    }
}
