package dev.minceraft.sonus.web.adapter.rtc;
// Created by booky10 in Sonus (4:26 AM 02.03.2026)

import dev.minceraft.sonus.common.natives.OpusNativesLoader;
import dev.minceraft.sonus.web.adapter.config.WebConfig;
import dev.minceraft.sonus.web.adapter.connection.WebSocketConnection;
import dev.minceraft.sonus.web.pion.PionApi;
import dev.minceraft.sonus.web.pion.launcher.PionLauncher;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@NullMarked
public final class RtcManager implements AutoCloseable {

    private final ScheduledExecutorService audioTicker = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r);
        thread.setName("webrtc_scheduler_" + System.identityHashCode(r));
        thread.setDaemon(true);
        return thread;
    });
    private final PionApi pion;

    private final OpusNativesLoader opusNatives;
    private final Supplier<WebConfig> config;

    private final Map<UUID, RtcHandler> peers = new ConcurrentHashMap<>();

    public RtcManager(OpusNativesLoader opusNatives, Supplier<WebConfig> config) {
        this.opusNatives = opusNatives;
        this.config = config;

        // run pion webrtc server
        this.pion = PionLauncher.launch().join();
    }

    public @Nullable RtcHandler getPeer(UUID playerId) {
        return this.peers.get(playerId);
    }

    public RtcHandler getPeer(WebSocketConnection connection) {
        UUID playerId = connection.getPlayer().getUniqueId();
        return this.peers.computeIfAbsent(playerId, __ -> {
            RtcHandler ret = new RtcHandler(this, connection);
            ret.startTicking(this.audioTicker);
            return ret;
        });
    }

    public boolean removePeer(UUID playerId) {
        try (RtcHandler handler = this.peers.remove(playerId)) {
            return handler != null;
        }
    }

    public PionApi getPion() {
        return this.pion;
    }

    public OpusNativesLoader getOpus() {
        return this.opusNatives;
    }

    public WebConfig getConfig() {
        return this.config.get();
    }

    @Override
    public void close() {
        this.peers.values().removeIf(handler -> {
            handler.disconnect("manager closed");
            return true;
        });
        this.audioTicker.close();
        this.pion.close();
    }
}
