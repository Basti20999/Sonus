package dev.minceraft.sonus.common;

public interface ISonusConfig {

    String getBind();

    int getPort();

    String getHost();

    int getMtuSize();

    double getVoiceChatRange();

    boolean allowRecordings();

    int getKeepAliveMs();
}
