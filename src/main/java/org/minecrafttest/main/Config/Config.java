package org.minecrafttest.main.Config;

import org.minecrafttest.main.ItemHandler;

import java.io.File;
public class Config {
    private final ItemHandler plugin;

    public Config() {
        this.plugin = ItemHandler.getPlugin();
    }

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

        File subConfigFile = new File(dataFolder + File.separator + "profiles", "profiles/subConfig.yml");
        if (!subConfigFile.exists()){
            plugin.saveResource("profiles/subConfig.yml",false);
        }

        plugin.reloadConfig();
    }
}