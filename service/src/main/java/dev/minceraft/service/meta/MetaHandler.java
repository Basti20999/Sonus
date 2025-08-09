package dev.minceraft.service.meta;
// Created by booky10 in Sonus (02:15 17.07.2025)

import dev.minceraft.service.SonusService;
import dev.minceraft.service.player.PlayerManager;
import dev.minceraft.service.player.SonusPlayer;
import dev.minceraft.sonus.protocol.meta.IMetaHandler;
import dev.minceraft.sonus.protocol.meta.servicebound.PlayerPositionsMessage;
import dev.minecraft.sonus.common.data.WorldVec3d;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.UUID;

@NullMarked
public class MetaHandler implements IMetaHandler {

    private final SonusService service;

    public MetaHandler(SonusService service) {
        this.service = service;
    }

    @Override
    public void handle(PlayerPositionsMessage message) {
        PlayerManager players = this.service.getPlayers();
        for (Map.Entry<UUID, WorldVec3d> entry : message.getPositions().entrySet()) {
            SonusPlayer player = players.getPlayer(entry.getKey());
            if (player != null) {
                player.setPosition(entry.getValue());
            }
        }
    }
}
