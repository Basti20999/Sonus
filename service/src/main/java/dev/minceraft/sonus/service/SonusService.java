package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:33 17.07.2025)

import dev.minceraft.sonus.common.ISonusConfig;
import dev.minceraft.sonus.common.ISonusService;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.service.adapter.AdapterManager;
import dev.minceraft.sonus.service.config.SonusConfig;
import dev.minceraft.sonus.service.config.YamlConfigHolder;
import dev.minceraft.sonus.service.meta.MetaDecoder;
import dev.minceraft.sonus.service.network.UdpServer;
import dev.minceraft.sonus.service.platform.IServicePlatform;
import dev.minceraft.sonus.service.player.PlayerManager;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class SonusService implements ISonusService {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final IServicePlatform platform;
    private final PlayerManager players = new PlayerManager(this);
    private final MetaDecoder metaDecoder = new MetaDecoder(this);
    private final SonusPluginMessenger pluginMessageListener = new SonusPluginMessenger(this);
    private final AdapterManager adapters = new AdapterManager();
    private final UdpServer udpServer = new UdpServer(this);

    private final YamlConfigHolder<SonusConfig> config;

    public SonusService(IServicePlatform platform) {
        this.platform = platform;
        this.config = new YamlConfigHolder<>(SonusConfig.class, this.platform.getConfigPath());
    }

    public void init() {
        LOGGER.info("Initializing Sonus Service...");
        this.udpServer.bind();
    }

    public IServicePlatform getPlatform() {
        return this.platform;
    }

    public PlayerManager getPlayers() {
        return this.players;
    }

    public MetaDecoder getMetaDecoder() {
        return this.metaDecoder;
    }

    @Override
    public SonusPluginMessenger getPluginMessenger() {
        return this.pluginMessageListener;
    }

    public AdapterManager getAdapters() {
        return this.adapters;
    }

    public ISonusConfig getConfig() {
        return this.config.getDelegate();
    }

    @Override
    public IUdpServer getUdpServer() {
        return this.udpServer;
    }
}
