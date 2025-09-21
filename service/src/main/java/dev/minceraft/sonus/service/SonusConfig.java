package dev.minceraft.sonus.service;
// Created by booky10 in Sonus (01:08 10.08.2025)

import dev.minceraft.sonus.common.ISonusConfig;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.net.InetSocketAddress;

@NullMarked
@ConfigSerializable
public class SonusConfig implements ISonusConfig {

    private InetSocketAddress bind = new InetSocketAddress("0.0.0.0", 9982);
    private InetSocketAddress host = new InetSocketAddress("127.0.0.1", 9982);
    private int mtuSize = 1412;
    private double voiceChatRange = 32.0;
    private boolean allowRecordings = false;
    private int keepAliveMs = 1000;
    private boolean autoGainControl = true;

    private SonusConfig() {
    }

    @Override
    public InetSocketAddress getBind() {
        return this.bind;
    }

    @Override
    public InetSocketAddress getHost() {
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

    @Override
    public boolean agcEnabled() {
        return this.autoGainControl;
    }
}
