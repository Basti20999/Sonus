package dev.minceraft.sonus.agent.paper.api;
// Created by booky10 in Sonus (18:11 17.11.2025)

import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SonusAgentApi {

    boolean isConnected(Player player);
}
