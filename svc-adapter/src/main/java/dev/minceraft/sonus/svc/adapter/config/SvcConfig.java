package dev.minceraft.sonus.svc.adapter.config;

import dev.minceraft.sonus.svc.protocol.data.Codec;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class SvcConfig {

    private Codec codec = Codec.VOIP;

    public Codec getCodec() {
        return this.codec;
    }
}
