package dev.minecraft.sonus.common.protocol.codec;

import dev.minecraft.sonus.common.protocol.util.PacketDirection;

public interface ServerBound extends IPacket {

    @Override
    default PacketDirection getDirection() {
        return PacketDirection.SERVERBOUND;
    }
}
