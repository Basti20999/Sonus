package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:33 17.07.2025)

import dev.minceraft.sonus.service.adapter.AdapterManager;
import dev.minceraft.sonus.service.meta.MetaDecoder;
import dev.minceraft.sonus.service.network.NetworkManager;
import dev.minceraft.sonus.service.player.PlayerManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SonusService {

    private final PlayerManager players = new PlayerManager();
    private final MetaDecoder metaDecoder = new MetaDecoder(this);
    private final AdapterManager adapters = new AdapterManager();
    private final NetworkManager networking = new NetworkManager(this);

    private final SonusConfig config;

    public PlayerManager getPlayers() {
        return this.players;
    }

    public MetaDecoder getMetaDecoder() {
        return this.metaDecoder;
    }

    public AdapterManager getAdapters() {
        return this.adapters;
    }

    public SonusConfig getConfig() {
        return this.config;
    }
}
