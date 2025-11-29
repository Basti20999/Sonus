package dev.minceraft.sonus.service.rooms;
// Created by booky10 in Sonus (19:35 15.11.2025)

import dev.minceraft.sonus.service.SonusService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

@NullMarked
public class TransientStaticRoom extends StaticRoom {

    public TransientStaticRoom(SonusService service, UUID roomId) {
        super(service, roomId);
    }

    @Override
    public boolean checkDiscarded(@Nullable Set<UUID> serverIds) {
        return this.members.isEmpty();
    }
}
