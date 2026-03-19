package com.cloudcheap.autologin.scheduler;

import org.bukkit.entity.Entity;

/**
 * Giao diện trừu tượng hóa Scheduler để tương thích cả Paper và Folia.
 */
public interface TaskScheduler {
    void runTask(Runnable runnable);
    void runTaskAsync(Runnable runnable);
    void runTaskAtEntity(Entity entity, Runnable runnable);
    void runTaskLater(Runnable runnable, long delay);
    void runTaskLaterAsync(Runnable runnable, long delay);
}
