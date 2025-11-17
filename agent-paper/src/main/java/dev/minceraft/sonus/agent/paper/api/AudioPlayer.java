package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (21:50 17.11.2025)

import dev.minceraft.sonus.agent.paper.audio.AudioSupplier;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

// inspired by https://github.com/henkelmax/simple-voice-chat/blob/f4ff1802317ee32869abed6fb08e9c63e0180602/bukkit/src/main/java/de/maxhenkel/voicechat/plugins/impl/audiochannel/AudioPlayerImpl.java
@NullMarked
public class AudioPlayer {

    private final UUID playerId;
    private final UUID channelId;
    private final AudioSupplier audio;
    private boolean started;
    private final AudioThread thread;

    public AudioPlayer(UUID playerId, UUID channelId, AudioSupplier audio) {
        this.playerId = playerId;
        this.channelId = channelId;
        this.audio = audio;
        this.thread = new AudioThread();
    }

    public void startPlaying() {
        if (!this.started) {
            this.thread.start();
            this.started = true;
        }
    }

    public void stopPlaying() {
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

    private final class AudioThread extends Thread {

        public AudioThread() {
            this.setDaemon(true);
            this.setName("AudioPlayer-" + AudioPlayer.this.channelId);
        }

        @Override
        public void run() {
            int framePosition = 0;
            long startTime = System.nanoTime();
            short[] frames = null;
            // TODO
        }
    }
}
