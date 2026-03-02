package dev.minceraft.sonus.web.adapter.connection;

import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.ISonusServer;
import dev.minceraft.sonus.web.adapter.WebAdapter;
import dev.minceraft.sonus.web.adapter.rtc.RtcHandler;
import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.Component.text;

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
    }

    public void sendConnected() {
        UUID playerId = this.player.getUniqueId();
        Component username = this.player.renderComponent(text(this.player.getName()));

        // send some info about the current server
        UUID serverId = this.player.getServerId();
        ISonusServer server = serverId != null ? this.adapter.getService().getPlayerManager().getServer(serverId) : null;
        Component serverName = server != null ? this.player.renderComponent(server.getName()) : null;
        String serverType = server != null ? server.getType() : null;

        this.sendPacket(new ConnectedPacket(playerId, username, serverId, serverName, serverType));
    }

    public void sendPacket(AbstractWebPacket<?> packet) {
        if (packet instanceof WebSocketPacket websocketPacket) {
            this.channel.writeAndFlush(packet);
        }
    }

    public AudioProcessor getProcessor(UUID channelId) {
        return this.processors.computeIfAbsent(channelId, __ ->
                this.adapter.getService().createAudioProcessor(AudioProcessor.Mode.VOICE));
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

    public WebAdapter getAdapter() {
        return this.adapter;
    }

    public WebSocketPacketHandler getWebSocketHandler() {
        return this.packetHandler;
    }

    public RtcHandler getRtc() {
        return this.adapter.getWebRtc().getPeer(this);
    }

    @Override
    public void close() {
        this.channel.close();
        this.processors.values().removeIf(processor -> {
            processor.close();
            return true;
        });
    }
}
