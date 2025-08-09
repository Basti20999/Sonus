package dev.minecraft.sonus.common.protocol.codec;

import dev.minecraft.sonus.common.protocol.util.PacketDirection;

public interface IPacket {

    PacketDirection getDirection();
}
