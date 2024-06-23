package org.minecrafttest.main.Watches;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;


public class Clock implements Runnable {
    private ScheduledTask task;

    public Clock(JavaPlugin plugin) {
    }

    @Override
    public void run() {

    }

    private TextColor getCurrentColor(long time) {
        int red, green, blue;

        if (time >= 0 && time < 12300) { // Morning to noon
            red = 255;
            green = 255;
            blue = 255;
        } else if (time >= 12300 && time < 13800) { // Noon to sunset
            float fraction = (time - 12300) / 1500f;
            red = 255;
            green = (int) (255 * (1 - fraction));
            blue = (int) (255 * (1 - fraction));
        } else if (time >= 13800 && time < 22800) { // Sunset to night
            red = 0;
            green = 0;
            blue = 0;
        } else { // Night to morning
            float fraction = (time - 22800) / 1200f;
            red = (int) (255 * fraction);
            green = (int) (255 * fraction);
            blue = (int) (255 * fraction);
        }

        return TextColor.color(red, green, blue);
    }
}