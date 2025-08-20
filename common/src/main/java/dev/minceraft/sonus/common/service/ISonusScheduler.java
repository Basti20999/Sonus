package dev.minceraft.sonus.common.service;

import java.util.concurrent.TimeUnit;

public interface ISonusScheduler {

    IScheduledTask schedule(Runnable task, long delay, long period, TimeUnit unit);

    IScheduledTask schedule(Runnable task, long delay, TimeUnit unit);
}
