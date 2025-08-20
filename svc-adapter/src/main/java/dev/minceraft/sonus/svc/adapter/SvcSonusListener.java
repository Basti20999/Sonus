package dev.minceraft.sonus.svc.adapter;

import dev.minceraft.sonus.common.service.ISonusServiceEvents;

import java.util.UUID;

public class SvcSonusListener implements ISonusServiceEvents {

    private final SvcAdapter adapter;

    public SvcSonusListener(SvcAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onPlayerSwitchBackend(UUID playerId) {
        this.adapter.getSessionManager().removeSession(playerId);
    }

    @Override
    public void onPlayerQuit(UUID playerId) {
        this.adapter.getSessionManager().removeSession(playerId);
    }
}
