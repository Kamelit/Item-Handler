package org.minecrafttest.main.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public MainConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void loadConfig() {
        try {
            plugin.reloadConfig();
            config = plugin.getConfig();
        } catch (Exception ex) {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            config = plugin.getConfig();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}