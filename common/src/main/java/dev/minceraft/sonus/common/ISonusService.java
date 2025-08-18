package dev.minceraft.sonus.common;

import dev.minceraft.sonus.common.protocol.tcp.IPluginMessenger;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;

public interface ISonusService {

    IUdpServer getUdpServer();

    IPluginMessenger getPluginMessenger();

    ISonusConfig getConfig();
}
