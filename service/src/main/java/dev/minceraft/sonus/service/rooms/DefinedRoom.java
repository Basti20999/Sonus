package dev.minceraft.sonus.service.rooms;

import dev.minceraft.sonus.common.IAudioSource;
import dev.minceraft.sonus.common.audio.SonusAudio;
import dev.minceraft.sonus.common.data.ISonusPlayer;
import dev.minceraft.sonus.common.rooms.options.RoomDefinition;
import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public class DefinedRoom extends AbstractRoom {

    private RoomDefinition definition;

    public DefinedRoom(SonusService service, UUID roomId, RoomDefinition definition) {
        super(service, roomId);
        this.definition = definition;
    }

    @Override
    protected void sendAudio0(IAudioSource source, SonusAudio audio) {
        for (ISonusPlayer receiver : this.members.values()) {
            if (receiver.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            RoomDefinition.RelationState state = this.definition.getState(source, receiver);
            switch (state) {
                case STATIC -> receiver.sendStaticAudio(source, audio);
                case SPATIAL -> receiver.sendSpatialAudio(source, audio);
                case SPATIAL_NORMALIZED -> receiver.sendSpatialNormedAudio(source, audio);
            }
        }
    }

    @Override
    protected void sendAudioEnd0(IAudioSource source, long sequence) {
        for (ISonusPlayer receiver : this.members.values()) {
            if (receiver.getSenderId().equals(source.getSenderId())) {
                continue;
            }
            RoomDefinition.RelationState state = this.definition.getState(source, receiver);
            switch (state) {
                case STATIC -> receiver.sendStaticAudioEnd(source, sequence);
                case SPATIAL, SPATIAL_NORMALIZED -> receiver.sendSpatialAudioEnd(source, sequence);
            }
        }
    }

    public void updateDefinition(RoomDefinition definition) {
        this.definition = definition;
    }
}
