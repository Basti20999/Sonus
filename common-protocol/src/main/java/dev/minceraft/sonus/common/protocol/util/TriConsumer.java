package dev.minceraft.sonus.common.protocol.util;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    void accept(T t, U u, V v);
}
