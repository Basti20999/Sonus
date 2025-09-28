package dev.minceraft.sonus.plasmo.protocol.tcp.data.source;


import dev.minceraft.sonus.plasmo.protocol.tcp.data.CodecInfo;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class EntitySourceInfo extends SourceInfo {

    private final int entityId;

    public EntitySourceInfo(ByteBuf buf) {
        super(buf, SourceType.ENTITY);
        this.entityId = buf.readInt();
    }

    public EntitySourceInfo(String addonId, UUID id, UUID voiceLineId, @Nullable String name,
                            byte state, @Nullable CodecInfo codecInfo, boolean stereo, boolean iconVisible, int angle,
                            int entityId) {
        super(SourceType.ENTITY, addonId, id, voiceLineId, name, state, codecInfo, stereo, iconVisible, angle);
        this.entityId = entityId;
    }

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        buf.writeInt(this.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }
}
