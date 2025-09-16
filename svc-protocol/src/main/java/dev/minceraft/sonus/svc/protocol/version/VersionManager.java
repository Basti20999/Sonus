package dev.minceraft.sonus.svc.protocol.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class VersionManager {

    public static final Set<Integer> SUPPORTED_VERSIONS = Set.of(18, 20);
    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    public static void logSupportedVersions() {
        LOGGER.info("Supported SVC protocol versions: {}", SUPPORTED_VERSIONS);
    }
}
