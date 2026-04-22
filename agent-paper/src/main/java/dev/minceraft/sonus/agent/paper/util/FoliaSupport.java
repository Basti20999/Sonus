package dev.minceraft.sonus.agent.paper.util;
// Created for Sonus - Folia detection utility

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FoliaSupport {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaSupport() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }
}
