package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:08 10.08.2025)

import dev.minceraft.sonus.common.ISonusConfig;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SonusConfig implements ISonusConfig {

    private String bind = "0.0.0.0";
    private int port = 9982;
    private String host = "localhost";
    private int mtuSize = 1412;
    private double voiceChatRange = 32.0;
    private boolean allowRecordings = false;
    private int keepAliveMs = 1000;

    private SonusConfig() {
    }

    @Override
    public String getBind() {
        return this.bind;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getMtuSize() {
        return this.mtuSize;
    }

    @Override
    public double getVoiceChatRange() {
        return this.voiceChatRange;
    }

    @Override
    public boolean allowRecordings() {
        return this.allowRecordings;
    }

    @Override
    public int getKeepAliveMs() {
        return this.keepAliveMs;
    }
}
