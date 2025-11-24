package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:33 17.07.2025)

import dev.minceraft.sonus.common.ISonusConfig;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.common.config.YamlConfigHolder;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.common.service.ISonusEventManager;
import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.common.service.ISonusScheduler;
import dev.minceraft.sonus.service.adapter.AdapterManager;
import dev.minceraft.sonus.service.agent.AgentManager;
import dev.minceraft.sonus.service.network.UdpServer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import dev.minceraft.sonus.service.player.PlayerManager;
import dev.minceraft.sonus.service.rooms.SonusRoomManager;
import dev.minceraft.sonus.service.server.SonusServer;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public final class SonusService implements ISonusService {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final IServicePlatform platform;
    private final PlayerManager players = new PlayerManager(this);
    private final SonusPluginMessenger pluginMessageListener = new SonusPluginMessenger(this);
    private final UdpServer udpServer = new UdpServer(this);
    private final SonusEventManager eventManager = new SonusEventManager(this);
    private final SonusScheduler scheduler = new SonusScheduler();
    private final SonusRoomManager roomManager = new SonusRoomManager(this);
    private final AdapterManager adapters = new AdapterManager(this);
    private final AgentManager agentManager = new AgentManager(this);

    // TODO periodically do cleanup
    private final Map<UUID, SonusServer> servers = new ConcurrentHashMap<>();
    private final YamlConfigHolder<SonusConfig> config;

    public SonusService(IServicePlatform platform) {
        this.platform = platform;
        this.config = new YamlConfigHolder<>(SonusConfig.class, this.platform.getDataPath().resolve("config.yml"));
    }

    public void init() {
        LOGGER.info("Initializing sonus service...");
        this.config.reloadConfig();
        this.adapters.init();
        this.roomManager.init();
        this.agentManager.init();
        this.udpServer.bind();
    }

    public void shutdown() {
        LOGGER.info("Shutting down sonus service...");
        this.udpServer.shutdown();
        this.scheduler.shutdown();
    }

    public IServicePlatform getPlatform() {
        return this.platform;
    }

    @Override
    public SonusPluginMessenger getPluginMessenger() {
        return this.pluginMessageListener;
    }

    public AdapterManager getAdapters() {
        return this.adapters;
    }

    @Override
    public ISonusConfig getConfig() {
        return this.config.getDelegate();
    }

    @Override
    public YamlConfigHolder<SonusConfig> getConfigHolder() {
        return this.config;
    }

    @Override
    public Path getDataDirectory() {
        return this.platform.getDataPath();
    }

    @Override
    public IUdpServer getUdpServer() {
        return this.udpServer;
    }

    @Override
    public ISonusEventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public ISonusScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public ISonusRoomManager getRoomManager() {
        return this.roomManager;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.players;
    }

    @Override
    public AudioProcessor createAudioProcessor(AudioProcessor.Mode mode) {
        return new AudioProcessor(() -> this.getConfig().getMtuSize(), mode);
    }

    public SonusServer getServer(UUID serverId) {
        return this.servers.computeIfAbsent(serverId, uuid ->
                new SonusServer(this, this.getPlatform().getServer(uuid)));
    }
}
