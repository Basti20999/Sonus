package dev.minceraft.sonus.common;

import java.net.InetSocketAddress;

public interface ISonusConfig {

    InetSocketAddress getBind();

    InetSocketAddress getHost();

    int getMtuSize();

    double getVoiceChatRange();

    boolean allowRecordings();

    int getKeepAliveMs();

    boolean agcEnabled();
}
