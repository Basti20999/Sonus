package dev.minceraft.sonus.common;

import dev.minceraft.sonus.common.service.ISonusRoomManager;
import dev.minceraft.sonus.common.service.ISonusEventManager;
import dev.minceraft.sonus.common.protocol.tcp.IPluginMessenger;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;
import dev.minceraft.sonus.common.service.ISonusScheduler;

import java.nio.file.Path;

public interface ISonusService {

    IUdpServer getUdpServer();

    IPluginMessenger getPluginMessenger();

    ISonusConfig getConfig();

    Path getDataDirectory();

    ISonusEventManager getEventManager();

    ISonusScheduler getScheduler();

    ISonusRoomManager getRoomManager();

    IPlayerManager getPlayerManager();
}
