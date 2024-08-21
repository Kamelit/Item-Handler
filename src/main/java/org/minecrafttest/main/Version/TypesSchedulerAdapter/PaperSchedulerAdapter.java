package org.minecrafttest.main.Version.TypesSchedulerAdapter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.minecrafttest.main.Version.SchedulerAdapter;

import java.util.concurrent.TimeUnit;

public class PaperSchedulerAdapter implements SchedulerAdapter {

    private BukkitTask task;

    @Override
    public void AsynchronousRunAtFixedRate(JavaPlugin plugin, Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        long periodTicks = timeUnit.toSeconds(period) * 20L;
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelay, periodTicks);
    }

    @Override
    public void AsynchronousRunDelayed(JavaPlugin plugin, Runnable runnable, long delay, TimeUnit timeUnit) {
        long delayTicks = timeUnit.toSeconds(delay) * 20L;
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
    }

    @Override
    public void RegionSchedulerRunDelayed(JavaPlugin plugin, Location location, Runnable runnable, long delayTicks) {
        runnable.run();
    }

    @Override
    public void RegionSchedulerExecute(JavaPlugin plugin, Location location, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void cancel() {
        task.cancel();
    }
}