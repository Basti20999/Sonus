package dev.minceraft.sonus.common.protocol.codec;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.registry.ProtocolMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IJsonCodec<H> extends ProtocolMessage<H> {

    void encode(JsonObject json);

    void decode(JsonObject json);
}
