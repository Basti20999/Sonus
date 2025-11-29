package dev.minceraft.sonus.agent.paper.events;
// Created by booky10 in Sonus (16:59 25.11.2025)

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class VoiceConnectionStateChangeEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final boolean connected;

    public VoiceConnectionStateChangeEvent(Player player, boolean connected) {
        super(player);
        this.connected = connected;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
