package dev.minceraft.sonus.protocol.svc.adapter;
// Created by booky10 in Sonus (02:19 10.08.2025)

import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import io.netty.buffer.ByteBuf;
import org.jspecify.annotations.NullMarked;

import java.net.InetSocketAddress;

@NullMarked
public class SvcProtocolAdapter implements VoiceProtocolAdapter {

    @Override
    public byte getMagicByte() {
        return (byte) 0b11111111;
    }

    @Override
    public void handleData(InetSocketAddress sender, ByteBuf buf) {
        // TODO
    }
}
