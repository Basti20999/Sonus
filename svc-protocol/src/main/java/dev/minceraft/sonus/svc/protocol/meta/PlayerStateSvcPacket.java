package dev.minceraft.sonus.svc.protocol.meta;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerStateSvcPacket extends SvcMetaPacket<PlayerStateSvcPacket> {

    private @MonotonicNonNull SvcPlayerState state;

    public PlayerStateSvcPacket() {
        super(SvcPluginChannels.PLAYER_STATE);
    }

    @Override
    public void encode(ByteBuf buf) {
        this.state.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf) {
        this.state = new SvcPlayerState(buf);
    }

    @Override
    public void encode(JsonObject json) {
        this.state.encode(json);
    }

    @Override
    public void decode(JsonObject json) {
        this.state = new SvcPlayerState(json);
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handlePlayerStatePacket(this);
    }

    public SvcPlayerState getState() {
        return this.state;
    }

    public void setState(SvcPlayerState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "PlayerStateSvcPacket{" +
                "state=" + state +
                '}';
    }
}
