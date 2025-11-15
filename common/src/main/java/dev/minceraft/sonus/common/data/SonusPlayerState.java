package dev.minceraft.sonus.common.data;

import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public record SonusPlayerState(UUID playerId, boolean tablistHidden, boolean hidden) {

    public void write(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        buf.writeBoolean(this.tablistHidden);
        buf.writeBoolean(this.hidden);
    }

    public static void write(ByteBuf buf, SonusPlayerState state) {
        state.write(buf);
    }

    public static SonusPlayerState read(ByteBuf buf) {
        UUID playerId = DataTypeUtil.readUniqueId(buf);
        boolean tablistHidden = buf.readBoolean();
        boolean hidden = buf.readBoolean();
        return new SonusPlayerState(playerId, tablistHidden, hidden);
    }
}
