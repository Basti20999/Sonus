package dev.minceraft.sonus.agent.paper.util.delta;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
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

    public synchronized void change(K key, V value) {
        V previous = this.state.put(key, value);
        if (!Objects.equals(previous, value)) {
            this.changed.put(key, value);
        }
    }

    @Nullable
    public synchronized V get(K key) {
        return this.state.get(key);
    }

    public synchronized void removeSilent(K key) {
        this.state.remove(key);
        this.changed.remove(key);
    }

    public synchronized Map<K, V> getChanges() {
        return this.changed;
    }

    public synchronized boolean isDirty() {
        return !this.changed.isEmpty();
    }

    public synchronized void clearChanges() {
        this.changed.clear();
    }

    /**
     * Atomically returns a copy of all pending changes and clears the change set.
     */
    public synchronized Map<K, V> drainChanges() {
        if (this.changed.isEmpty()) {
            return Map.of();
        }
        Map<K, V> copy = new HashMap<>(this.changed);
        this.changed.clear();
        return copy;
    }
}
