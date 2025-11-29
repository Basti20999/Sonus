package dev.minceraft.sonus.web.adapter.connection;

import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.web.adapter.WebAdapter;
import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketConnection implements AutoCloseable {

    private final WebAdapter adapter;
    private final ISonusPlayer player;
    private final Channel channel;

    private final WebSocketPacketHandler packetHandler = new WebSocketPacketHandler(this);
    private final Map<UUID, AudioProcessor> processors = new ConcurrentHashMap<>();
    private int version = -1;

    public WebSocketConnection(WebAdapter adapter, ISonusPlayer player, Channel channel) {
        this.adapter = adapter;
        this.player = player;
        this.channel = channel;
        this.player.setAdapater(this.adapter);
    }

    public ISonusPlayer getPlayer() {
        return this.player;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void sendPacket(AbstractWebPacket<?> packet) {
        if (packet instanceof WebSocketPacket websocketPacket) {
            this.sendWebSocketPacket(websocketPacket);
        }
    }

    private void sendWebSocketPacket(WebSocketPacket packet) {
        this.channel.writeAndFlush(packet);
    }

    public boolean isConnected() {
        return this.player.isConnected();
    }

    public void setConnected(boolean connected) {
        this.player.setConnected(connected);
    }

    public AudioProcessor getProcessor(UUID channelId) {
        return this.processors.computeIfAbsent(channelId, __ ->
                this.adapter.getService().createAudioProcessor(AudioProcessor.Mode.VOICE));
    }

    public WebAdapter getAdapter() {
        return adapter;
    }

    public WebSocketPacketHandler getWebSocketHandler() {
        return this.packetHandler;
    }

    @Override
    public void close() {
        this.processors.values().removeIf(processor -> {
            processor.close();
            return true;
        });
    }
}
