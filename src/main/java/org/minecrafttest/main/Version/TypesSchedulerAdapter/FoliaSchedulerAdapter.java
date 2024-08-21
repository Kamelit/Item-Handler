package org.minecrafttest.main.Version.TypesSchedulerAdapter;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecrafttest.main.Version.SchedulerAdapter;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {

    private ScheduledTask scheduledTask;

    @Override
    public void AsynchronousRunAtFixedRate(JavaPlugin plugin, Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> runnable.run(), initialDelay, period, timeUnit);
    }

    @Override
    public void AsynchronousRunDelayed(JavaPlugin plugin, Runnable runnable, long delay, TimeUnit timeUnit) {
        scheduledTask = Bukkit.getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), delay, timeUnit);
    }

    @Override
    public void RegionSchedulerRunDelayed(JavaPlugin plugin, Location location, Runnable runnable, long delayTicks) {
        scheduledTask = Bukkit.getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delayTicks);
    }

    @Override
    public void RegionSchedulerExecute(JavaPlugin plugin, Location location, Runnable runnable) {
        Bukkit.getRegionScheduler().execute(plugin, location, runnable);
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
    }
}