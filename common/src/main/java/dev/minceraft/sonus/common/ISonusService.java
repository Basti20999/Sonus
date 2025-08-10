package dev.minceraft.sonus.common;

import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.protocol.udp.IUdpServer;

import java.util.UUID;

public interface ISonusService {

    IUdpServer getUdpServer();

    ISonusPlayer getPlayer(UUID playerId);
}
