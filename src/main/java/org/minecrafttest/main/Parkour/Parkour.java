package org.minecrafttest.main.Parkour;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Particles.TypesAnimation;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;

import java.io.File;
import java.util.*;

public class Parkour {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final Config config = plugin.getCustomConfig();
    private final File file = new File(plugin.getDataFolder(),"parkour/parkour.yml");
    public YamlConfiguration parkourConfiguration = YamlConfiguration.loadConfiguration(file);

    private final Map<String, List<Checkpoint>> checkpointsMap = new HashMap<>();
    private final Map<Player, Checkpoint> playerLastCheckpoint = new HashMap<>();
    private final Map<String, Set<Player>> playerParkourMap = new HashMap<>();
    private final Set<Player> players = new HashSet<>();

    public void loadCheckpoints() {
        for (String path : parkourConfiguration.getKeys(false)) {
            ConfigurationSection config = parkourConfiguration.getConfigurationSection(path);
            List<Checkpoint> checkpoints = new ArrayList<>();
            if (config != null) {
                for (String key : config.getKeys(false)) {
                    String worldName = config.getString(key + ".world");
                    World world = Bukkit.getWorld(Objects.requireNonNull(worldName));
                    int x = config.getInt(key + ".x");
                    int y = config.getInt(key + ".y");
                    int z = config.getInt(key + ".z");
                    int minY = config.getInt(key + ".min_y", Integer.MIN_VALUE);
                    int maxY = config.getInt(key + ".max_y", Integer.MAX_VALUE);
                    if (world != null) {
                        Location location = new Location(world, x, y, z);
                        checkpoints.add(new Checkpoint(path , key ,location, minY, maxY));
                    }
                }
            }
            checkpointsMap.put(path, checkpoints);
        }
    }

    public void loadAnimationChecks(Player player){
        String parkourName = plugin.getParkour().getPlayerParkour(player);

        if (parkourName != null && plugin.getChronometer().isRunChronometer(player)) {
            List<Checkpoint> checkpoints = getCheckpointsMap().get(parkourName);
            plugin.getParticleAnimation().playAnimation(parkourName, checkpoints,TypesAnimation.BEAUTIFUL_CHECKPOINT);
        }
    }

    public void ClearCheckpoints(){
        checkpointsMap.clear();
    }

    public Map<String, List<Checkpoint>> getCheckpointsMap() {
        return checkpointsMap;
    }

    public Map<Player, Checkpoint> getPlayerLastCheckpoint(){
        return playerLastCheckpoint;
    }

    public void setPlayerParkour(Player player, String parkourName) {
        players.add(player);
        playerParkourMap.put(parkourName, players);
    }

    public String getPlayerParkour(Player player) {
        for (Map.Entry<String, Set<Player>> entry : playerParkourMap.entrySet()){
            String pl = entry.getKey();
            if (entry.getValue().contains(player)) return pl;
        }
        return null;
    }

    public void removePlayerParkour(Player player, String parkourName) {
        playerParkourMap.get(parkourName).remove(player);
    }

    public boolean RegisterParkourName(String PrincipalPath){
        if (!parkourConfiguration.contains(PrincipalPath)){
            parkourConfiguration.set(PrincipalPath,"");
            config.saveWorldConfig(parkourConfiguration,file);
            return true;
        }
        return false;
    }

    public boolean RegisterParkour(String PrincipalPath, String world , int x, int y, int z){
        if (parkourConfiguration.contains(PrincipalPath)){
            int Checkpoint_Number = 1;

            while (parkourConfiguration.contains(PrincipalPath+".Checkpoint_"+Checkpoint_Number)){
                Checkpoint_Number++;
            }
            String name = PrincipalPath+".Checkpoint_"+Checkpoint_Number;


            parkourConfiguration.set(name+".world", world);
            parkourConfiguration.set(name+".x",x);
            parkourConfiguration.set(name+".y",y);
            parkourConfiguration.set(name+".z",z);
            config.saveWorldConfig(parkourConfiguration,file);
            return true;
        }
        return false;
    }


    public void RegisterParkourAllFallInY(String PrincipalPath, int Y_value, String type, CommandSender sender) {
        ConfigurationSection section = parkourConfiguration.getConfigurationSection(PrincipalPath);
        MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();
        messageBuilder.append(PrincipalPath + ": ");
        if (section != null) {
            for (String paths : section.getKeys(false)) {
                section.set(paths + "." + type, Y_value);
                messageBuilder.append(" " + paths + ": " + Y_value, ColorText.GOLD);
            }
            config.saveWorldConfig(parkourConfiguration, file);
            messageBuilder.build();
            messageBuilder.senderMessage(sender);
        }
    }

    public boolean RegisterParkourFallInY(String PrincipalPath, String Checkpoint, int Y_value, String type, boolean isMin) {
        String path = PrincipalPath + "." + Checkpoint;
        int registered_Y = parkourConfiguration.getInt(path + ".y");
        boolean shouldUpdate = isMin ? Y_value < registered_Y : Y_value > registered_Y;

        if (shouldUpdate) {
            parkourConfiguration.set(path + "." + type, Y_value);
            config.saveWorldConfig(parkourConfiguration, file);
            return true;
        }
        return false;
    }

    //Next
    public void RemoveCheckPointPlayer(String key, Player player) {
        for (List<Checkpoint> checkpoints : checkpointsMap.values()) {
            for (Checkpoint checkpoint : checkpoints) {
                Set<Player> players = checkpoint.getPlayers().get(key);
                if (players != null) {
                    //System.out.println("Before: " + players);
                    players.removeIf(p -> p.equals(player));
                    //System.out.println("After: " + players);
                }
            }
        }
    }
}