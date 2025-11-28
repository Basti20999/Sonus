package dev.minceraft.sonus.svc.protocol.meta.clientbound;


import dev.minceraft.sonus.svc.protocol.SvcPacketContext;
import dev.minceraft.sonus.svc.protocol.data.SvcPlayerState;
import dev.minceraft.sonus.svc.protocol.meta.IMetaSvcHandler;
import dev.minceraft.sonus.svc.protocol.meta.SvcMetaPacket;
import dev.minceraft.sonus.svc.protocol.util.SvcPluginChannels;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public class PlayerStatesSvcPacket extends SvcMetaPacket {

    private @MonotonicNonNull Map<UUID, SvcPlayerState> states;

    public PlayerStatesSvcPacket() {
        super(SvcPluginChannels.PLAYER_STATES);
    }

    @Override
    public void encode(ByteBuf buf, SvcPacketContext ctx) {
        buf.writeInt(this.states.size());
        for (SvcPlayerState value : this.states.values()) {
            value.encode(buf);
        }
    }

    @Override
    public void decode(ByteBuf buf, SvcPacketContext ctx) {
        int size = buf.readInt();
        this.states = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            SvcPlayerState state = new SvcPlayerState(buf);
            this.states.put(state.getPlayerId(), state);
        }
    }

    @Override
    public void handle(IMetaSvcHandler handler) {
        handler.handlePlayerStatesPacket(this);
    }

    public Map<UUID, SvcPlayerState> getStates() {
        return this.states;
    }

    public void setStates(Map<UUID, SvcPlayerState> states) {
        this.states = states;
    }
}
