package dev.minceraft.sonus.svc.protocol.data;

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
}
