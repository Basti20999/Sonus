package dev.minceraft.sonus.common.protocol.tcp;

public interface IPmListener {

    void registerCodec(AbstractPluginMessageCodec codec);
}
