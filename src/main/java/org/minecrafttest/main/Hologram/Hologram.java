package org.minecrafttest.main.Hologram;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.minecrafttest.main.Cache.CacheManager;
import org.minecrafttest.main.Cache.types.ArmorStand.ArmorStandData;
import org.minecrafttest.main.Database.Database;
import org.minecrafttest.main.ItemHandler;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Hologram {

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
            .character('&')
            .build();
    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final YamlConfiguration yamlconfiguration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "hologram/hologram.yml"));
    public final double verticalSpaces = yamlconfiguration.getDouble("config.space", 0.50);
    private final Database database = plugin.getDatabase();
    private static final String METADATA_KEY = "hologram";

    private final CacheManager cacheManager = new CacheManager(plugin);
    private final List<ArmorStandData> temp = new ArrayList<>();
    private final List<Location> locations = new ArrayList<>();

    public void init() {

        Optional<List<ArmorStandData>> data = Optional.ofNullable(cacheManager.get(METADATA_KEY));

        AtomicInteger counter = new AtomicInteger(0);

        data.ifPresent(cache -> cache.forEach(armorStandData -> Bukkit.getRegionScheduler().runDelayed(plugin, armorStandData.getLocation(), task -> {
            Entity entity = Bukkit.getEntity(armorStandData.getUuid());
            if (entity != null && counter.get() == 0){
                Component message1 = Component.text().append(Component.text("[" + plugin.getName() + "] --->", NamedTextColor.DARK_AQUA)).build();
                Bukkit.getConsoleSender().sendMessage(message1);
                cache.forEach(action->{
                    Component message = Component.text()
                            .append(Component.text("[CacheManager] ", NamedTextColor.GOLD))
                            .append(Component.text("UUID found in "+ action.getUuid()))
                            .append(Component.text(" __cache--> ", NamedTextColor.RED))
                            .append(Component.text(action.getLocation().toString(), NamedTextColor.YELLOW))
                            .build();
                    Bukkit.getConsoleSender().sendMessage(message);
                });
            }
            if (entity != null && Objects.requireNonNull(Bukkit.getWorld(armorStandData.getLocation().getWorld().getName())).getEntities().contains(entity)) {
                entity.remove();
                Component message = Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_AQUA))
                        .append(Component.text("[CacheManager] ", NamedTextColor.GOLD))
                        .append(Component.text("Entity removed: "+ entity.getUniqueId()))
                        .build();
                Bukkit.getConsoleSender().sendMessage(message);
            }
            if (counter.get() == cache.size()-1) {
                cacheManager.remove(METADATA_KEY);
                temp.clear();
            }
            counter.incrementAndGet();
        },20L)));


        final ConfigurationSection hologram = yamlconfiguration.getConfigurationSection("holograms");
        if (hologram == null) return;

        for (String path : hologram.getKeys(false)) {
            ConfigurationSection config = yamlconfiguration.getConfigurationSection("holograms." + path);
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

            spawn(location, text, path, end_text_down.isPresent());
        }
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
            Bukkit.getRegionScheduler().runDelayed(plugin, location, t -> {
                if (__packets && value == linesText.size() - 1) {
                    Location local = location.clone();
                    local.subtract(0, verticalSpaces * (linesText.size()+1) , 0);
                    locations.add(local);
                }
                if (endLine && value == linesText.size() - 1) location.subtract(0, verticalSpaces, 0);
                ArmorStand armorStand = world.spawn(location, ArmorStand.class);
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setInvisible(true);
                armorStand.setMarker(true);
                Component text = serializer.deserialize(line);
                armorStand.customName(text);
                armorStand.setCustomNameVisible(true);
                armorStand.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, true));
                temp.add(new ArmorStandData(armorStand.getUniqueId(), armorStand.getEntityId(), armorStand.getLocation()));
                location.subtract(0, verticalSpaces, 0);
                if (value == linesText.size()-1){
                    cacheManager.put(METADATA_KEY, temp);
                }
            }, 40L);
            i++;
        }
    }

    public List<Location> getLocations (){
        return locations;
    }
}
