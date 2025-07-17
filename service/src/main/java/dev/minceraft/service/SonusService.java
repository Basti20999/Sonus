package dev.minceraft.service;
// Created by booky10 in Sonus (01:33 17.07.2025)

import dev.minceraft.service.meta.MetaDecoder;
import dev.minceraft.service.player.PlayerManager;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SonusService {

    private final PlayerManager players = new PlayerManager();
    private final MetaDecoder metaDecoder = new MetaDecoder(this);

    public PlayerManager getPlayers() {
        return this.players;
    }

    public MetaDecoder getMetaDecoder() {
        return this.metaDecoder;
    }
}
