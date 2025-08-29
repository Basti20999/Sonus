package dev.minceraft.sonus.svc.protocol.data;

import dev.minceraft.sonus.common.rooms.RoomType;
import net.kyori.adventure.util.Index;

public enum SonusGroupType {

    NORMAL("normal"),
    OPEN("open"),
    ISOLATED("isolated");

    public static final Index<String, SonusGroupType> ID_INDEX = Index.create(
            SonusGroupType.class, SonusGroupType::getId);

    private final String id;

    SonusGroupType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public RoomType toSonus() {
        return switch (this) {
            case OPEN -> RoomType.OPEN;
            case ISOLATED -> RoomType.ISOLATED;
            case NORMAL -> RoomType.NORMAL;
        };
    }

    public static SonusGroupType fromSonus(RoomType type) {
        return switch (type) {
            case OPEN -> OPEN;
            case ISOLATED -> ISOLATED;
            case NORMAL -> NORMAL;
        };
    }
}
