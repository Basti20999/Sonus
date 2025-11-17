package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (23:54 16.11.2025)

import dev.minceraft.sonus.agent.paper.SonusAgentPlugin;
import dev.minceraft.sonus.agent.paper.audio.AudioSupplier;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.protocol.meta.servicebound.RegisterAudioCategoryMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
@NullMarked
public class SonusAgentApiImpl implements SonusAgentApi{

    protected final SonusAgentPlugin plugin;

    protected final Set<UUID> connectedPlayers = ConcurrentHashMap.newKeySet();

    public SonusAgentApiImpl(SonusAgentPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isConnected(Player player) {
        return this.connectedPlayers.contains(player.getUniqueId());
    }

    @Override
    public void registerAudioCategory(AudioCategory category) {
        RegisterAudioCategoryMessage packet = new RegisterAudioCategoryMessage();
        packet.setCategory(category);
        this.plugin.onDefinition(packet);
    }

    @Override
    public AudioPlayer createAudioPlayer(Player player, UUID channelId, @Nullable UUID categoryId, AudioSupplier audio) {
        return null;
    }

    public Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }
}
