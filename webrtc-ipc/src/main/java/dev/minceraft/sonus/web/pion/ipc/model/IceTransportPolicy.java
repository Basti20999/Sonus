package dev.minceraft.sonus.web.pion.ipc.model;
// Created by booky10 in Sonus (4:47 PM 08.03.2026)

import org.jspecify.annotations.NullMarked;

/**
 * Determines which ICE candidates get sent to the web application.
 */
@NullMarked
public enum IceTransportPolicy {

    ALL,
    ONLY_RELAY,
    NO_HOSTS,
}
