package dev.minceraft.sonus.svc.protocol.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class VersionManager {

    public static final int V_18 = 18;
    public static final int V_20 = 20;

    public static final int OLDEST_VERSION = V_18;

    public static final Set<Integer> SUPPORTED_VERSIONS = Set.of(V_18, V_20);

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    public static void logSupportedVersions() {
        LOGGER.info("Supported SVC protocol versions: {}", SUPPORTED_VERSIONS);
    }
}
