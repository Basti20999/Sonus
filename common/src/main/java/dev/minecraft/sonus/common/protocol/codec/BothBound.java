package dev.minecraft.sonus.common.protocol.codec;

import dev.minecraft.sonus.common.protocol.util.PacketDirection;

public interface BothBound extends IPacket {

    void setDirection(PacketDirection direction);
}
