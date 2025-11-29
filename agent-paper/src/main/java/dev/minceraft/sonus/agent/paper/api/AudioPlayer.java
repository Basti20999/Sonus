package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (21:50 17.11.2025)

import dev.minceraft.sonus.agent.paper.audio.AudioTicker;
import dev.minceraft.sonus.protocol.meta.IMetaMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage;
import dev.minceraft.sonus.protocol.meta.servicebound.AudioStreamMessage.Frame;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

// inspired by https://github.com/henkelmax/simple-voice-chat/blob/f4ff1802317ee32869abed6fb08e9c63e0180602/bukkit/src/main/java/de/maxhenkel/voicechat/plugins/impl/audiochannel/AudioPlayerImpl.java
@NullMarked
public final class AudioPlayer {

    private boolean started;
    private final AudioThread thread;

    public AudioPlayer(
            UUID playerId, UUID channelId, @Nullable UUID categoryId,
            AudioTicker ticker, Consumer<IMetaMessage> messageConsumer
    ) {
        this.thread = new AudioThread(playerId, channelId, categoryId, ticker, messageConsumer);
    }

    public void startPlaying() {
        if (!this.started) {
            this.thread.start();
            this.started = true;
        }
    }

    public void stopPlaying() {
        this.thread.stop = true;
        this.thread.interrupt();
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isPlaying() {
        return this.thread.isAlive();
    }

    public boolean isStopped() {
        return this.started && !this.thread.isAlive();
    }

    private static final class AudioThread extends Thread {

        private final AudioStreamMessage packet = new AudioStreamMessage();
        private final AudioTicker ticker;
        private final Consumer<IMetaMessage> messageConsumer;
        private boolean stop = false;

        public AudioThread(
                UUID playerId, UUID channelId, @Nullable UUID categoryId,
                AudioTicker ticker, Consumer<IMetaMessage> messageConsumer
        ) {
            this.packet.setPlayerId(playerId);
            this.packet.setChannelId(channelId);
            this.packet.setCategoryId(categoryId);
            this.ticker = ticker;
            this.messageConsumer = messageConsumer;

            this.setDaemon(true);
            this.setName("Sonus-AudioPlayer-" + channelId);
        }

        @Override
        public void run() {
            try {
                List<Frame> frames;
                while (!this.stop && !(frames = this.ticker.get()).isEmpty()) {
                    // fire audio packet with frames
                    this.packet.setFrames(frames);
                    this.messageConsumer.accept(this.packet);
                    // wait until next frame tick
                    this.ticker.waitNextTick();
                }
            } finally {
                this.ticker.close();
            }
        }
    }
}
