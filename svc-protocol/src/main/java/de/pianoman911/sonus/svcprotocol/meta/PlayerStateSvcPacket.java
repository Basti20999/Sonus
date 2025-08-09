package de.pianoman911.sonus.svcprotocol.meta;

import com.google.gson.JsonObject;
import de.pianoman911.sonus.svcprotocol.data.SonusPlayerState;
import de.pianoman911.sonus.svcprotocol.util.SvcPluginChannels;
import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.ClientBound;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerStateSvcPacket extends SvcMetaPacket<PlayerStateSvcPacket> implements ClientBound {

    private @MonotonicNonNull SonusPlayerState state;

    public PlayerStateSvcPacket() {
        super(SvcPluginChannels.PLAYER_STATE);
    }

    @Override
    public void encode(ByteBuf buf) {
        this.state.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.state = new SonusPlayerState(buf);
    }

    @Override
    public void encode(JsonObject json, int version) {
        this.state.encode(json);
    }

    @Override
    public void decode(JsonObject json) {
        this.state = new SonusPlayerState(json);
    }

    @Override
    public void handle(ISonusPlayer player, IMetaSvcHandler handler) {
        handler.handlePlayerStatePacket(player, this);
    }

    public SonusPlayerState getState() {
        return this.state;
    }

    public void setState(SonusPlayerState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PlayerStateSvcPacket{" +
                "state=" + state +
                '}';
    }
}
