package org.minecrafttest.main.Cache;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;
import org.minecrafttest.main.Cache.types.ArmorStand.ArmorStandData;
import org.minecrafttest.main.Cache.types.ArmorStand.ArmorStandDataTypeAdapter;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CacheManager {
    private final File cacheFile;
    private final Gson gson;
    private final Logger logger;
    private final Plugin plugin;
    private Map<String, List<ArmorStandData>> cache;
    static private final String FILE_NAME = "plugin_cache.json";

    public CacheManager(Plugin plugin) {
        this.plugin = plugin;
        this.cacheFile = new File(plugin.getDataFolder(), "cache/"+ FILE_NAME);
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithModifiers(java.lang.reflect.Modifier.STATIC, java.lang.reflect.Modifier.TRANSIENT);
        gsonBuilder.registerTypeAdapter(ArmorStandData.class, new ArmorStandDataTypeAdapter());
        this.gson = gsonBuilder.create();
        this.logger = plugin.getLogger();
        this.cache = new HashMap<>();

        if (cacheFile.exists()) {
            loadCache();
        }
    }

    private void loadCache() {
        try (FileReader reader = new FileReader(cacheFile)) {
            Type type = new TypeToken<Map<String, List<ArmorStandData>>>() {}.getType();
            cache = gson.fromJson(reader, type);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    public void saveCache() {
        try (FileWriter writer = new FileWriter(cacheFile)) {
            gson.toJson(cache, writer);
            MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();

            messageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                    .append("[CacheManager] ", ColorText.GOLD)
                    .append("Cache saved or update file: " + FILE_NAME)
                    .build();
            messageBuilder.BukkitSender();
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    public void put(String key, List<ArmorStandData> value) {
        cache.put(key, value);
        saveCache();
    }

    public List<ArmorStandData> get(String key) {
        return cache.get(key);
    }   

    public void remove(String key) {
        cache.remove(key);
        if (cache.isEmpty()) {
            if (!cacheFile.delete()) {
                logger.severe("Failed to delete cache file");
            }

            MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();
            messageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                    .append("[CacheManager] ", ColorText.GOLD)
                    .append("Cache remove: " + FILE_NAME)
                    .build();
            messageBuilder.BukkitSender();
        } else {
            saveCache();

            MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();
            messageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                    .append("[CacheManager] ", ColorText.GOLD)
                    .append("Cache remove tree " + key)
                    .build();
            messageBuilder.BukkitSender();
        }
    }
}