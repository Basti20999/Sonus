package dev.minceraft.sonus.common.events;

public interface ISonusEventManager extends IServiceEvents {

    void registerListener(IServiceEvents events);
}
