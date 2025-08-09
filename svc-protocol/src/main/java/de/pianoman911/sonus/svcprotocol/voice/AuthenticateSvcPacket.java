package de.pianoman911.sonus.svcprotocol.voice;

import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ServerBound;
import dev.minecraft.sonus.common.protocol.util.DataTypeUtil;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class AuthenticateSvcPacket extends SvcVoicePacket<AuthenticateSvcPacket> implements ServerBound {

    private @MonotonicNonNull UUID playerId;
    private @MonotonicNonNull UUID secret;

    public AuthenticateSvcPacket() {
    }

    @Override
    public void encode(ByteBuf buf) {
        DataTypeUtil.writeUniqueId(buf, this.playerId);
        DataTypeUtil.writeUniqueId(buf, this.secret);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.playerId = DataTypeUtil.readUniqueId(buf);
        this.secret = DataTypeUtil.readUniqueId(buf);
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleAuthenticate(player, this);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID getSecret() {
        return this.secret;
    }

    public void setSecret(UUID secret) {
        this.secret = secret;
    }
}
