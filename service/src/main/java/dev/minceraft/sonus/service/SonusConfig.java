package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:08 10.08.2025)

import org.jspecify.annotations.NullMarked;

@NullMarked
public class SonusConfig {

    private String bind = "0.0.0.0";
    private int port = 9982;
    private String host = "localhost";

    private SonusConfig() {
    }

    public String getBind() {
        return this.bind;
    }

    public int getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }
}
