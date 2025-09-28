package dev.minceraft.sonus.plasmo.protocol.tcp;

import dev.minceraft.sonus.plasmo.protocol.tcp.clientbound.TcpClientBoundHandler;
import dev.minceraft.sonus.plasmo.protocol.tcp.serverbound.TcpServerBoundHandler;

public interface TcpHandler extends TcpClientBoundHandler, TcpServerBoundHandler {
}
