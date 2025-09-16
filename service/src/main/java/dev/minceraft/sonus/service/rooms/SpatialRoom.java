package dev.minceraft.sonus.service.rooms;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.data.SonusPlayerState;
import dev.minceraft.sonus.common.data.WorldVec3d;
import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SpatialRoom extends AbstractRoom {

    private double maxDistanceSquared;

    public SpatialRoom(SonusService service) {
        service.getConfigHolder().addReloadHookAndRun(config -> {
            this.maxDistanceSquared = config.getVoiceChatRange();
            this.maxDistanceSquared *= this.maxDistanceSquared;
        });
    }

    @Override
    protected void sendAudio0(IAudioSource source, SonusAudio audio) {
        WorldVec3d sourcePos = source.getPosition();
        if (sourcePos == null) {
            this.handlePositionless(source, audio);
            return;
        }

        for (ISonusPlayer member : this.members.values()) {
            if (member.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            WorldVec3d listenerPos = member.getPosition();
            if (listenerPos == null) {
                continue; // Ignore
            }
            double distance = sourcePos.distanceSquared(listenerPos);
            if (distance > this.maxDistanceSquared) {
                continue;
            }
            SonusPlayerState states = member.getPerPlayerStates().get(source.getSenderId());
            if (states != null && states.hidden()) {
                continue; // Ignore hidden players
            }

            member.sendSpatialAudio(source, audio);
        }
    }

    private void handlePositionless(IAudioSource source, SonusAudio audio) {
        for (ISonusPlayer member : this.members.values()) {
            if (member.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            SonusPlayerState states = member.getPerPlayerStates().get(source.getSenderId());
            if (states != null && states.hidden()) {
                continue; // Ignore hidden players
            }
            member.sendStaticAudio(source, audio);
        }
    }
}
