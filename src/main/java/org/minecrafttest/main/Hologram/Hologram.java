package org.minecrafttest.main.Hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.minecrafttest.main.Cache.CacheManager;
import org.minecrafttest.main.Cache.types.ArmorStand.ArmorStandData;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.Database.Database;
import org.minecrafttest.main.Hologram.ScoresHologram.Packets.CustomHologram;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Version.SchedulerAdapter;
import org.minecrafttest.main.Version.ArmorBuilder;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Hologram {

    private final ArmorBuilder armorBuilder = ArmorBuilder.createArmorBuilder().SerializerCodesColor('&');
    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final File file = new File(plugin.getDataFolder(), "hologram/hologram.yml");
    public final YamlConfiguration yamlconfiguration = YamlConfiguration.loadConfiguration(file);
    public final double verticalSpaces = yamlconfiguration.getDouble("config.space", 0.50);
    private final Database database = plugin.getDatabase();
    //private static final String METADATA_KEY = "hologram";

    private final CacheManager cacheManager = new CacheManager(plugin);
    private final HashMap<String, List<ArmorStandData>> temp = new HashMap<>();
    private final Config config = plugin.getCustomConfig();
    public final CustomHologram customHologram = new CustomHologram();

    public void init() {

        final ConfigurationSection hologram = yamlconfiguration.getConfigurationSection("holograms");
        if (hologram == null) return;
        hologram.getKeys(false).forEach(METADATA_KEY->{
            System.out.println(METADATA_KEY);
            Optional<List<ArmorStandData>> data = Optional.ofNullable(cacheManager.get(METADATA_KEY));

            AtomicInteger counter = new AtomicInteger(0);

            //data.ifPresent(cache -> cache.forEach(armorStandData -> Bukkit.getRegionScheduler().runDelayed(plugin, armorStandData.getLocation(), task -> {},20L)));

            data.ifPresent(cache -> cache.forEach(armorStandData ->  SchedulerAdapter.createSchedulerApi().RegionSchedulerRunDelayed(plugin, armorStandData.getLocation(), () -> {
                Entity entity = getEntityByUUID(Objects.requireNonNull(Bukkit.getWorld(armorStandData.getLocation().getWorld().getName())), armorStandData.getUuid());
                if (entity != null && counter.get() == 0){
                    MessageBuilder message1Builder = MessageBuilder.createMessageBuilder();
                    message1Builder.append("[" + plugin.getName() + "] --->", ColorText.DARK_AQUA)
                            .build();
                    message1Builder.BukkitSender();

                    cache.forEach(action->{
                        MessageBuilder cacheMessageBuilder = MessageBuilder.createMessageBuilder();
                        cacheMessageBuilder.append("[CacheManager] ", ColorText.GOLD)
                                .append("UUID found in " + action.getUuid())
                                .append(" __cache--> ", ColorText.RED)
                                .append(action.getLocation().toString(), ColorText.YELLOW)
                                .build();
                        cacheMessageBuilder.BukkitSender();
                    });
                }
                if (entity != null && Objects.requireNonNull(Bukkit.getWorld(armorStandData.getLocation().getWorld().getName())).getEntities().contains(entity)) {
                    entity.remove();
                    MessageBuilder cacheManagerMessageBuilder = MessageBuilder.createMessageBuilder();
                    cacheManagerMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                            .append("[CacheManager] ", ColorText.GOLD)
                            .append("Entity removed: " + entity.getUniqueId())
                            .build();
                    cacheManagerMessageBuilder.BukkitSender();

                }
                if (counter.get() == cache.size()-1) {
                    cacheManager.remove(METADATA_KEY);
                    temp.clear();
                }
                counter.incrementAndGet();
            },20L)));

            ConfigurationSection config = hologram.getConfigurationSection(METADATA_KEY);
            if (config == null) return;

            String worldName = config.getString("world");
            double x = config.getDouble("x");
            double z = config.getDouble("z");
            double y = config.getDouble("y");
            World world = Bukkit.getWorld(Objects.requireNonNull(worldName));

            List<String> text = new ArrayList<>();
            Optional<String> Title = Optional.ofNullable(config.getString("title"));
            Title.ifPresent(text::add);
            Optional<List<String>> field_text = Optional.of(config.getStringList("field_text"));
            field_text.ifPresent(text::addAll);
            boolean showDatabaseScores = config.getBoolean("database_scores", false);
            if (showDatabaseScores) {
                int a = config.getInt("num_dat_a", 0);
                int b = config.getInt("num_dat_b", 0);
                text.addAll(database.getListInTo(a, b));
            }
            Optional<String> end_text_up = Optional.ofNullable(config.getString("end_text_up"));
            end_text_up.ifPresent(text::add);
            Optional<String> end_text_down = Optional.ofNullable(config.getString("end_text_down"));
            end_text_down.ifPresent(text::add);

            final Location location = new Location(world, x, z, y);

            spawn(location, text, METADATA_KEY, end_text_down.isPresent());
        });


    }
    public void spawn(final Location location, List<String> linesText, String path, boolean endLine) {
        int i = 0;
        boolean __packets = yamlconfiguration.getBoolean("holograms." + path + ".__custom__name_score", false);
        World world = location.getWorld();
        if (world == null) {
            plugin.getLogger().severe("World is null for location: " + location);
            return;
        }
        for (String line : linesText) {
            final int value = i;

            SchedulerAdapter.createSchedulerApi().RegionSchedulerRunDelayed(plugin, location, ()->{
                if (__packets && value == linesText.size() - 1) {
                    Location local = location.clone();
                    local.subtract(0, verticalSpaces * (linesText.size()+1) , 0);
                    customHologram.keyLocation(path,local);
                }
                if (endLine && value == linesText.size() - 1) location.subtract(0, verticalSpaces, 0);
                ArmorStand armorStand = world.spawn(location, ArmorStand.class);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                //armorStand.setInvisible(true);
                armorStand.setMarker(true);
                armorBuilder.CustomNameArmor(armorStand, line);
                temp.computeIfAbsent(path, k -> new ArrayList<>()).add(new ArmorStandData(armorStand.getUniqueId(), armorStand.getEntityId(), armorStand.getLocation()));
                location.subtract(0, verticalSpaces, 0);
                if (value == linesText.size()-1){
                    cacheManager.put(path, temp.get(path));
                }
            }, 40L);
            //Bukkit.getRegionScheduler().runDelayed(plugin, location, t -> {}, 40L);
            i++;
        }
    }

    public Entity getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    public void teleportingHologram(String key, Location newBaseLocation) {
        List<ArmorStandData> armorStandDataList = cacheManager.get(key);
        if (armorStandDataList == null) {
            plugin.getLogger().warning("No holograms found for key: " + key);
            return;
        }

        boolean __packets = yamlconfiguration.getBoolean("holograms." + key + ".__custom__name_score", false);
        boolean endLine = Optional.ofNullable(yamlconfiguration.getString("holograms." + key + ".end_text_down")).isPresent();

        if (__packets) newBaseLocation.add(0, (armorStandDataList.size() * verticalSpaces + 1), 0);
        else newBaseLocation.add(0, (armorStandDataList.size() * verticalSpaces), 0);


        final ConfigurationSection hologram = yamlconfiguration.getConfigurationSection("holograms." + key);
        if (hologram != null) {
            hologram.set("x", newBaseLocation.getX());
            hologram.set("y", newBaseLocation.getY());
            hologram.set("z", newBaseLocation.getZ());
            hologram.set("world", newBaseLocation.getWorld().getName());
            config.saveWorldConfig(yamlconfiguration, file);
        }

        int value = 0;
        for (ArmorStandData armorStandData : armorStandDataList) {
            final int val = value;
            SchedulerAdapter.createSchedulerApi().RegionSchedulerExecute(plugin, armorStandData.getLocation(), () -> {

                if (__packets && val == armorStandDataList.size() - 1) {
                    Location updatedLocation = newBaseLocation.clone();
                    updatedLocation.subtract(0, verticalSpaces * (armorStandDataList.size() + 1), 0);
                    customHologram.UpdatePacketsLocation(key, updatedLocation);

                }
                if (endLine && val == armorStandDataList.size() - 1) newBaseLocation.subtract(0, verticalSpaces, 0);
                
                Entity entity = getEntityByUUID(armorStandData.getLocation().getWorld(), armorStandData.getUuid());
                if (entity instanceof ArmorStand) {
                    entity.teleportAsync(newBaseLocation);
                }
                armorStandData.setLocation(newBaseLocation);
                newBaseLocation.subtract(0, verticalSpaces, 0);

                if (val == armorStandDataList.size()-1){
                    cacheManager.remove(key);
                    cacheManager.put(key, armorStandDataList);
                }
            });
            value++;
        }
    }

}
