package dev.minceraft.sonus.web.pion.ipc.model;
// Created by booky10 in Sonus (9:23 PM 06.03.2026)

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record IceServer(
        String url,
        @Nullable String user,
        @Nullable String auth
) {
}
