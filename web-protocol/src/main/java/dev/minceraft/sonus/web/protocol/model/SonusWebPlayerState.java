package dev.minceraft.sonus.web.protocol.model;
// Created by booky10 in Sonus (20:36 28.11.2025)

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.rooms.IRoom;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@NullMarked
public class SonusWebPlayerState {

    private static final int FLAG_MUTED = 1 << 0;
    private static final int FLAG_DEAFENED = 1 << 1;
    private static final int FLAG_HAS_GROUP = 1 << 2;

    private final UUID uniqueId;
    private final Component name;
    private final boolean muted;
    private final boolean deafened;
    private final @Nullable UUID primaryRoomId;

    public SonusWebPlayerState(
            UUID uniqueId, Component name,
            boolean muted, boolean deafened,
            @Nullable UUID primaryRoomId
    ) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.muted = muted;
        this.deafened = deafened;
        this.primaryRoomId = primaryRoomId;
    }

    public static SonusWebPlayerState fromState(ISonusPlayer player, ISonusPlayer viewer) {
        UUID uniqueId = player.getUniqueId();
        TextComponent name = Component.text(player.getName(viewer));
        boolean muted = player.isMuted();
        boolean deafened = player.isDeafened();
        IRoom primaryRoom = player.getPrimaryRoom();
        UUID primaryRoomId = primaryRoom != null ? primaryRoom.getId() : null;
        return new SonusWebPlayerState(uniqueId, name, muted, deafened, primaryRoomId);
    }

    public static void encode(ByteBuf buf, SonusWebPlayerState state) {
        DataTypeUtil.writeUniqueId(buf, state.uniqueId);
        DataTypeUtil.writeComponentJson(buf, state.name);
        buf.writeByte(state.packFlags());
        if (state.primaryRoomId != null) {
            DataTypeUtil.writeUniqueId(buf, state.primaryRoomId);
        }
    }

    public static SonusWebPlayerState decode(ByteBuf buf) {
        UUID uniqueId = DataTypeUtil.readUniqueId(buf);
        Component name = DataTypeUtil.readComponentJson(buf);
        short flags = buf.readUnsignedByte();
        boolean muted = (flags & FLAG_MUTED) != 0;
        boolean deafened = (flags & FLAG_DEAFENED) != 0;
        boolean hasGroup = (flags & FLAG_HAS_GROUP) != 0;
        UUID groupId = hasGroup ? DataTypeUtil.readUniqueId(buf) : null;
        return new SonusWebPlayerState(uniqueId, name, muted, deafened, groupId);
    }

    public byte packFlags() {
        return (byte) (0
                | (this.muted ? FLAG_MUTED : 0)
                | (this.deafened ? FLAG_DEAFENED : 0)
                | (this.primaryRoomId != null ? FLAG_HAS_GROUP : 0));
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public Component getName() {
        return this.name;
    }

    public boolean isMuted() {
        return this.muted;
    }

    public boolean isDeafened() {
        return this.deafened;
    }

    public @Nullable UUID getPrimaryRoomId() {
        return this.primaryRoomId;
    }
}
