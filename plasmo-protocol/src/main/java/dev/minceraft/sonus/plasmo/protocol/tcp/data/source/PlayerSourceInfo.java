package dev.minceraft.sonus.plasmo.protocol.tcp.data.source;


import dev.minceraft.sonus.plasmo.protocol.tcp.data.CodecInfo;
import dev.minceraft.sonus.plasmo.protocol.tcp.data.VoicePlayerInfo;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class PlayerSourceInfo extends SourceInfo {

    private final VoicePlayerInfo playerInfo;

    public PlayerSourceInfo(ByteBuf buf) {
        super(buf, SourceType.PLAYER);
        this.playerInfo = new VoicePlayerInfo(buf);
    }

    public PlayerSourceInfo(String addonId, UUID id, UUID voiceLineId, @Nullable String name,
                            byte state, @Nullable CodecInfo codecInfo, boolean stereo, boolean iconVisible, int angle,
                            VoicePlayerInfo playerInfo) {
        super(SourceType.PLAYER, addonId, id, voiceLineId, name, state, codecInfo, stereo, iconVisible, angle);
        this.playerInfo = playerInfo;
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        this.playerInfo.write(buf);
    }

    public VoicePlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }
}
