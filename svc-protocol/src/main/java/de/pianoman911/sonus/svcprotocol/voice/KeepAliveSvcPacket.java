package de.pianoman911.sonus.svcprotocol.voice;

import dev.minecraft.sonus.common.data.ISonusPlayer;
import dev.minecraft.sonus.common.protocol.codec.BothBound;
import dev.minecraft.sonus.common.protocol.util.PacketDirection;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KeepAliveSvcPacket extends SvcVoicePacket<KeepAliveSvcPacket> implements BothBound {

    private @MonotonicNonNull PacketDirection direction;

    public KeepAliveSvcPacket(){
    }

    @Override
    public PacketDirection getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(PacketDirection direction) {
        this.direction = direction;
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }

    @Override
    public void handle(ISonusPlayer player, IVoiceSvcHandler handler) {
        handler.handleKeepAlivePacket(player, this);
    }
}
