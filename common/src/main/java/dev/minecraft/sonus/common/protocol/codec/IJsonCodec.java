package dev.minecraft.sonus.common.protocol.codec;

import com.google.gson.JsonObject;
import dev.minecraft.sonus.common.protocol.registry.ProtocolMessage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IJsonCodec<C, H> extends ProtocolMessage<C, H> {

    void encode(JsonObject json);

    void decode(JsonObject json);
}
