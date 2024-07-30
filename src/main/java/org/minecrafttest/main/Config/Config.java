package org.minecrafttest.main.Config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.minecrafttest.main.ItemHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

//Configurations_yaml's
public class Config {
    private final ItemHandler plugin = ItemHandler.getPlugin();


    public boolean getClearInventory(){
        return this.plugin.getConfig().getBoolean("clear_inventory",false);
    }

    public boolean getChangeHand(){
        return this.plugin.getConfig().getBoolean("change_hand",true);
    }

    public boolean getDeleteDuplicateMetaItems(){
        return this.plugin.getConfig().getBoolean("delete_duplicate_meta_items",true);
    }


    public void loadConfig() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            boolean success = dataFolder.mkdir();
            if (!success) {
                plugin.getLogger().warning("Failed to create plugin data folder!");
                return;
            }
        }

        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        File subConfigFile = new File(dataFolder, "profiles/subConfig.yml");
        if (!subConfigFile.exists()){
            plugin.saveResource("profiles/subConfig.yml",false);
        }

        File worldFile = new File(new File(plugin.getDataFolder(), "blocks_events"), "world.yml");
        if (!worldFile.exists()) {
            plugin.saveResource("blocks_events/world.yml", false);
        }

        File parkour = new File(new File(plugin.getDataFolder(), "parkour"), "parkour.yml");
        if (!parkour.exists()) {
            plugin.saveResource("parkour/parkour.yml", false);
        }

        File hologram = new File(new File(plugin.getDataFolder(), "hologram"), "hologram.yml");
        if (!hologram.exists()) {
            plugin.saveResource("hologram/hologram.yml", false);
        }

        plugin.reloadConfig();
    }

    public void saveWorldConfig(YamlConfiguration yml, File file) {
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "An error occurred while saving world configuration!", e);
        }
    }

}