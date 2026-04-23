package dev.minceraft.sonus.plasmo.protocol;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class PlasmoPmChannels {

    public static final String NAMESPACE = "plasmo";

    private static final Set<Key> MUTABLE_CHANNELS = new HashSet<>();
    public static final Set<Key> CHANNELS = Collections.unmodifiableSet(MUTABLE_CHANNELS);

    public static final Key CHANNEL = registerChannel("voice/v2");
    public static final Key SERVICE_CHANNEL = registerChannel("voice/v2/service");

    private PlasmoPmChannels() {
    }

    private static Key registerChannel(@KeyPattern.Value String name) {
        Key key = Key.key(NAMESPACE, name);
        MUTABLE_CHANNELS.add(key);
        return key;
    }
}
