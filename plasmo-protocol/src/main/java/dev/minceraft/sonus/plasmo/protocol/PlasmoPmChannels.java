package dev.minceraft.sonus.plasmo.protocol;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

import java.util.HashSet;
import java.util.Set;

public final class PlasmoPmChannels {

    public static final String NAMESPACE = "plasmo";

    public static final Set<Key> CHANNELS = new HashSet<>();

    public static final Key CHANNEL = registerChannel("voice/v2");
    public static final Key SERVICE_CHANNEL = registerChannel("voice/v2/service");

    private static Key registerChannel(@KeyPattern.Value String name) {
        Key key = Key.key(NAMESPACE, name);
        CHANNELS.add(key);
        return key;
    }
}
