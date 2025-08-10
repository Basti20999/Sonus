package dev.minceraft.sonus.service.adapter;
// Created by booky10 in Sonus (01:45 10.08.2025)

import dev.minceraft.sonus.common.adapter.VoiceAdapter;
import dev.minceraft.sonus.common.adapter.VoiceProtocolAdapter;
import net.kyori.adventure.util.Services;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@NullMarked
public final class AdapterManager {

    private final Set<VoiceAdapter> adapters = Services.services(VoiceAdapter.class);
    private final @Nullable VoiceProtocolAdapter[] adaptersByMagic = new VoiceProtocolAdapter[0xFF];

    public AdapterManager() {
        for (VoiceAdapter adapter : this.adapters) {
            VoiceProtocolAdapter proto = adapter.getProtocolAdapter();
            this.adaptersByMagic[proto.getMagicByte() & 0xFF] = proto;
        }
    }

    public @Nullable VoiceProtocolAdapter getAdapter(byte magicByte) {
        return this.adaptersByMagic[magicByte & 0xFF];
    }

    public Set<VoiceAdapter> getAdapters() {
        return this.adapters;
    }
}
