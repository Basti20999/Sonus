package de.pianoman911.sonus.svcprotocol.util;

import net.kyori.adventure.key.Key;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class SvcPluginChannels {

    private static final Set<Key> PACKETS = new HashSet<>();
    private static final String NAMESPACE = "voicechat";

    public static final Key INIT = register(Key.key(NAMESPACE, "init"));
    public static final Key SECRET = register(Key.key(NAMESPACE, "secret"));
    public static final Key REQUEST_SECRET = register(Key.key(NAMESPACE, "request_secret"));
    public static final Key PLAYER_STATE = register(Key.key(NAMESPACE, "player_state"));
    public static final Key PLAYER_STATES = register(Key.key(NAMESPACE, "player_states"));
    public static final Key SET_GROUP = register(Key.key(NAMESPACE, "set_group"));
    public static final Key CREATE_GROUP = register(Key.key(NAMESPACE, "create_group"));
    public static final Key ADD_GROUP = register(Key.key(NAMESPACE, "add_group"));
    public static final Key REMOVE_GROUP = register(Key.key(NAMESPACE, "remove_group"));
    public static final Key LEAVE_GROUP = register(Key.key(NAMESPACE, "leave_group"));
    public static final Key JOINED_GROUP = register(Key.key(NAMESPACE, "joined_group"));
    public static final Key ADD_CATEGORY = register(Key.key(NAMESPACE, "add_category"));
    public static final Key REMOVE_CATEGORY = register(Key.key(NAMESPACE, "remove_category"));
    public static final Key UPDATE_STATE = register(Key.key(NAMESPACE, "update_state"));

    private SvcPluginChannels() {
    }

    private static Key register(Key key) {
        PACKETS.add(key);
        return key;
    }

    public static Set<Key> getPackets() {
        return Set.copyOf(PACKETS);
    }

    public static boolean contains(Key key) {
        return PACKETS.contains(key);
    }

    public static void consumePackets(Consumer<Key> consumer) {
        for (Key packet : PACKETS) {
            consumer.accept(packet);
        }
    }
}
