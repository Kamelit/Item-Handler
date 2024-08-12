package org.minecrafttest.main.Version;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecrafttest.main.Version.TypesSchedulerAdapter.FoliaSchedulerAdapter;
import org.minecrafttest.main.Version.TypesSchedulerAdapter.PaperSchedulerAdapter;

import java.util.concurrent.TimeUnit;

public interface SchedulerAdapter {

    void AsynchronousRunAtFixedRate(JavaPlugin plugin, Runnable runnable, long initialDelay, long period, TimeUnit timeUnit);
    void AsynchronousRunDelayed(JavaPlugin plugin, Runnable runnable, long delay, TimeUnit timeUnit);
    void RegionSchedulerRunDelayed(JavaPlugin plugin, Location location, Runnable runnable, long delayTicks);
    void cancel();

    static SchedulerAdapter createSchedulerApi(){
        if (APICompatibility.isFoliaApi()) {
            return new FoliaSchedulerAdapter();
        }else {
            return new PaperSchedulerAdapter();
        }
    }
}