package dev.minceraft.sonus.agent.paper.util.delta;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Objects;
import java.util.function.Supplier;

public class DeltaTrackerTable<R, C, V> {

    private final Table<R, C, V> state;
    private final Table<R, C, V> changed;

    public DeltaTrackerTable(Supplier<Table<R, C, V>> holderSupplier) {
        this.state = holderSupplier.get();
        this.changed = holderSupplier.get();
    }

    public synchronized void change(R row, C column, V value) {
        V previous = this.state.put(row, column, value);
        if (!Objects.equals(previous, value)) {
            this.changed.put(row, column, value);
        }
    }

    public synchronized V get(R row, C column) {
        return this.state.get(row, column);
    }

    public synchronized void computeIfAbsent(R row, C column, Supplier<V> valueSupplier) {
        if (this.state.contains(row, column)) {
            return;
        }
        V value = valueSupplier.get();
        this.state.put(row, column, value);
        this.changed.put(row, column, value);
    }

    public synchronized void removeSilent(R row, C column) {
        this.state.remove(row, column);
        this.changed.remove(row, column);
    }

    public synchronized void removeRowSilent(R row) {
        this.state.row(row).clear();
        this.changed.row(row).clear();
    }

    public synchronized void removeColumnSilent(C column) {
        this.state.column(column).clear();
        this.changed.column(column).clear();
    }

    public synchronized Table<R, C, V> getChanges() {
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
    public synchronized Table<R, C, V> drainChanges() {
        if (this.changed.isEmpty()) {
            return HashBasedTable.create();
        }
        Table<R, C, V> copy = HashBasedTable.create(this.changed);
        this.changed.clear();
        return copy;
    }
}
