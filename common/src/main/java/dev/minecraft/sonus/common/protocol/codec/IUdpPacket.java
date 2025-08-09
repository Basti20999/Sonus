package dev.minecraft.sonus.common.protocol.codec;


import dev.minecraft.sonus.common.protocol.udp.AbstractMagicUdpCodec;

import java.net.InetSocketAddress;

public interface IUdpPacket extends IPacket {

    AbstractMagicUdpCodec<?> getCodec();

    InetSocketAddress getRemoteAddress();

    void setRemoteAddress(InetSocketAddress remoteAddress);
}
