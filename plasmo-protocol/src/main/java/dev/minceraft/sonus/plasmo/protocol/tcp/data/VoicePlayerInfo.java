package dev.minceraft.sonus.plasmo.protocol.tcp.data;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class VoicePlayerInfo {

    private final UUID uniqueId;
    private final String name;
    private final boolean muted;
    private final boolean voiceDisabled;
    private final boolean microphoneDisabled;

    public VoicePlayerInfo(ByteBuf buf) {
        this.uniqueId = DataTypeUtil.readUniqueId(buf);
        this.name = Utf8String.readUnsignedShort(buf);
        this.muted = buf.readBoolean();
        this.voiceDisabled = buf.readBoolean();
        this.microphoneDisabled = buf.readBoolean();
    }

    public VoicePlayerInfo(UUID uniqueId, String name, boolean muted, boolean voiceDisabled, boolean microphoneDisabled) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.muted = muted;
        this.voiceDisabled = voiceDisabled;
        this.microphoneDisabled = microphoneDisabled;
    }

    public void write(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.uniqueId);
        Utf8String.writeUnsignedShort(buf, this.name);
        buf.writeBoolean(this.muted);
        buf.writeBoolean(this.voiceDisabled);
        buf.writeBoolean(this.microphoneDisabled);
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getName() {
        return this.name;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public boolean isVoiceDisabled() {
        return this.voiceDisabled;
    }

    public boolean isMicrophoneDisabled() {
        return this.microphoneDisabled;
    }
}
