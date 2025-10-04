package dev.minceraft.sonus.plasmo.protocol.udp;

import dev.minceraft.sonus.plasmo.protocol.udp.bothbound.UdpBothBoundHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.clientbound.UdpClientBoundHandler;
import dev.minceraft.sonus.plasmo.protocol.udp.serverbound.UdpServerBoundHandler;

public interface UdpHandler extends UdpBothBoundHandler, UdpClientBoundHandler, UdpServerBoundHandler {
}
