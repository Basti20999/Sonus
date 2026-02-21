package dev.minceraft.sonus.agent.paper.util.delta;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class DeltaTrackerMap<K, V> {

    public Map<K, V> state;
    public Map<K, V> changed;

    public DeltaTrackerMap(Supplier<Map<K, V>> holderSupplier) {
        this.state = holderSupplier.get();
        this.changed = holderSupplier.get();
    }

    public void change(K key, V value) {
        V previous = this.state.put(key, value);
        if (Objects.equals(previous, value)) {
            return;
        }
        this.changed.put(key, value);
    }

    @Nullable
    public V get(K key) {
        return this.state.get(key);
    }

    public void removeSilent(K key) {
        this.state.remove(key);
        this.changed.remove(key);
    }

    public Map<K, V> getChanges() {
        return this.changed;
    }

    public boolean isDirty() {
        return !this.changed.isEmpty();
    }

    public void clearChanges() {
        this.changed.clear();
    }
}
