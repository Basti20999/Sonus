package dev.minceraft.sonus.web.adapter.connection;

import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.ISonusServer;
import dev.minceraft.sonus.web.adapter.WebAdapter;
import dev.minceraft.sonus.web.adapter.rtc.RtcHandler;
import dev.minceraft.sonus.web.protocol.AbstractWebPacket;
import dev.minceraft.sonus.web.protocol.packets.WebSocketPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.ConnectedPacket;
import dev.minceraft.sonus.web.protocol.packets.clientbound.VoiceActivityPacket;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class WebSocketConnection implements AutoCloseable {

    private final WebAdapter adapter;
    private final ISonusPlayer player;
    private final Channel channel;

    private final WebSocketPacketHandler packetHandler = new WebSocketPacketHandler(this);
    private int version = -1;
    private boolean voiceActive = false;

    public WebSocketConnection(WebAdapter adapter, ISonusPlayer player, Channel channel) {
        this.adapter = adapter;
        this.player = player;
        this.channel = channel;
    }

    public void handleAudioInput(SonusAudio audio) {
        // our web app doesn't keep track of whether the user itself is speaking, so just do this for now
        if (!this.voiceActive) {
            this.voiceActive = true;
            this.sendPacket(new VoiceActivityPacket(this.player.getUniqueId(), true));
        }
        this.player.handleAudioInput(audio);
    }

    public void handleAudioInputEnd(long sequence) {
        // see above for reason
        if (this.voiceActive) {
            this.voiceActive = false;
            this.sendPacket(new VoiceActivityPacket(this.player.getUniqueId(), false));
        }
        this.player.handleAudioInputEnd(sequence);
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
        if (packet instanceof WebSocketPacket) {
            this.channel.writeAndFlush(packet);
        }
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
    }
}
