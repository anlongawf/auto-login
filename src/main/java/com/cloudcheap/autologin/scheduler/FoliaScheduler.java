package com.cloudcheap.autologin.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler implements TaskScheduler {
    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runTask(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        Bukkit.getAsyncScheduler().runNow(plugin, t -> runnable.run());
    }

    @Override
    public void runTaskAtEntity(Entity entity, Runnable runnable) {
        entity.getScheduler().run(plugin, t -> runnable.run(), null);
    }

    @Override
    public void runTaskLater(Runnable runnable, long delay) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), delay);
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long delay) {
        Bukkit.getAsyncScheduler().runDelayed(plugin, t -> runnable.run(), delay * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
