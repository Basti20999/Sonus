package dev.minceraft.sonus.common.protocol.util;

@FunctionalInterface
public interface ObjIntObjectConsumer<T, U> {

    void accept(T t, int value, U u);
}