package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (23:54 16.11.2025)

import com.google.common.base.Suppliers;
import dev.minceraft.sonus.agent.paper.SonusAgentPlugin;
import dev.minceraft.sonus.agent.paper.audio.AudioSupplier;
import dev.minceraft.sonus.agent.paper.audio.AudioTicker;
import dev.minceraft.sonus.common.audio.AudioCategory;
import dev.minceraft.sonus.common.audio.AudioProcessor;
import dev.minceraft.sonus.protocol.meta.servicebound.RegisterAudioCategoryMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static dev.minceraft.sonus.common.SonusConstants.FRAME_SIZE;
import static dev.minceraft.sonus.common.SonusConstants.SAMPLE_RATE;

@ApiStatus.Internal
@NullMarked
public class SonusAgentApiImpl implements SonusAgentApi {

    private static final int AUDIO_TICKER_FRAMES = (SAMPLE_RATE / FRAME_SIZE); // only tick once per second

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
    public AudioPlayer createAudioPlayer(
            Player player, UUID channelId, @Nullable UUID categoryId,
            AudioSupplier audio, AudioProcessor.Mode mode
    ) {
        AudioProcessor processor = this.createAudioProcessor(mode);
        AudioTicker ticker = new AudioTicker(audio, processor, AUDIO_TICKER_FRAMES);
        return new AudioPlayer(
                player.getUniqueId(), channelId, categoryId,
                ticker, this.plugin::sendMetaPacket
        );
    }

    @Override
    public AudioProcessor createAudioProcessor(AudioProcessor.Mode mode) {
        return new AudioProcessor(() -> 1412, Suppliers.ofInstance(mode)); // TODO config for mtu
    }

    public Set<UUID> getConnectedPlayers() {
        return this.connectedPlayers;
    }
}
