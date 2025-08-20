package dev.minceraft.sonus.common;

import dev.minceraft.sonus.common.events.ISonusEventManager;
import dev.minceraft.sonus.common.protocol.tcp.IPluginMessenger;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;

import java.nio.file.Path;

public interface ISonusService {

    IUdpServer getUdpServer();

    IPluginMessenger getPluginMessenger();

    ISonusConfig getConfig();

    Path getDataDirectory();

    ISonusEventManager getEventManager();
}
