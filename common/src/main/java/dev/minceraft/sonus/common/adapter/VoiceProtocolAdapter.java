package dev.minceraft.sonus.common.adapter;
// Created by booky10 in Sonus (01:41 10.08.2025)

import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;

@NullMarked
public interface VoiceProtocolAdapter {

    byte getMagicByte();

    void handleData(InetSocketAddress sender, ByteBuf buf);
}
