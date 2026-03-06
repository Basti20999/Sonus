package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (4:26 AM 02.03.2026)

import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Version;
import org.freedesktop.gstreamer.glib.GLib;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@NullMarked
public final class RtcManager implements AutoCloseable {

    static {
        GLib.setEnv("GST_DEBUG", "4", true);
        Gst.init(Version.of(1, 16));
    }

    private final ScheduledExecutorService audioTicker = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("webrtc_scheduler_" + System.identityHashCode(r));
        thread.setDaemon(true);
        return thread;
    });
    private final Supplier<WebConfig> config;

    private final Map<UUID, RtcHandler> peers = new HashMap<>();

    public RtcManager(Supplier<WebConfig> config) {
        this.config = config;
    }

    public @Nullable RtcHandler getPeer(UUID playerId) {
        return this.peers.get(playerId);
    }

    public RtcHandler getPeer(WebSocketConnection connection) {
        UUID playerId = connection.getPlayer().getUniqueId();
        return this.peers.computeIfAbsent(playerId, __ ->
                this.configureRtcHandler(new RtcHandler(this, connection)));
    }

    private RtcHandler configureRtcHandler(RtcHandler handler) {
        for (WebConfig.IceServerConfig iceServer : this.config.get().iceServers) {
            handler.addTurnServer(iceServer.url(), iceServer.user(), iceServer.auth());
        }
        handler.initialize(this.audioTicker);
        return handler;
    }

    public boolean removePeer(UUID playerId) {
        try (RtcHandler handler = this.peers.remove(playerId)) {
            return handler != null;
        }
    }

    @Override
    public void close() {
        this.peers.values().removeIf(handler -> {
            handler.disconnect("manager closed");
            return true;
        });
        this.audioTicker.close();
        Gst.quit();
    }
}
