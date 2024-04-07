package org.minecrafttest.main.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;

import java.io.File;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//GameCommandExecutor
public class GameCommandExecutor implements CommandExecutor, TabCompleter {
    private final ItemHandler plugin;
    private final PlayerInteractionListener listener;
    private final Config config;
    private String configName = "";
    private File file;
    private YamlConfiguration worldConfig;
    private Location location;
    private double x, y, z;
    //private final List<String> actions = new ArrayList<>(Arrays.asList("a","b","c","d","e"));

    public GameCommandExecutor() {
        this.plugin = ItemHandler.getPlugin();
        this.listener = plugin.getListener();
        this.config = plugin.getCustomConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            Component Message = Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_GRAY))
                    .append(Component.text("Only players can Execute This Command ", NamedTextColor.DARK_GRAY))
                    .build();
            sender.sendMessage(Message);
            return true;
        }


        if (args.length == 0 ) {
            Component Message = Component.text()
                    .append(Component.text("Use ", NamedTextColor.AQUA))
                    .append(Component.text("/"+ label +" help", NamedTextColor.BLUE))
                    .append(Component.text(" for view details.", NamedTextColor.AQUA))
                    .build();
            sender.sendMessage(Message);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendCommandList(sender, label);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            String file = !Objects.equals(configName, "") ? configName : "subConfig";
            listener.updates("profiles/" + file);

            Component enableMessage = Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.BLUE))
                    .append(Component.text("Reload Config! ", NamedTextColor.WHITE))
                    .build();
            Bukkit.getConsoleSender().sendMessage(enableMessage);
            sender.sendMessage(enableMessage);
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("load")) {
            StringBuilder configNameBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                configNameBuilder.append(args[i]);
                if (i < args.length - 1) {
                    configNameBuilder.append(" ");
                }
            }
            configName = configNameBuilder.toString();
            File configFile = new File(plugin.getDataFolder() + File.separator + "profiles", configName + ".yml");
            if (!configFile.exists()) {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("YML not Found ", NamedTextColor.RED))
                        .append(Component.text(configName + ".yml", NamedTextColor.RED))
                        .build());
                return true;
            }
            listener.updates("profiles/" + configName);
            Component enableMessage = Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.BLUE))
                    .append(Component.text("Config loaded: " + configName, NamedTextColor.WHITE))
                    .build();
            sender.sendMessage(enableMessage);
            return true;
        }

        if (args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
            try {
                Player targetPlayer = Bukkit.getPlayer(args[3]);
                if (targetPlayer != null) {
                    x = targetPlayer.getLocation().getX();
                    y = targetPlayer.getLocation().getY();
                    z = targetPlayer.getLocation().getZ();
                    if (args.length >= 6){
                        String nameWorld = args[4];

                        final int index = 5;
                        if (getMarks(sender, args, nameWorld, index)) return true;
                    }else {
                        sender.sendMessage(Component.text()
                                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                                .append(Component.text("Incorrect Syntax's", NamedTextColor.RED))
                                .build());
                    }
                }else {
                    if (args[3].startsWith("~")) {
                        String offsetString = args[3].substring(1);
                        if (offsetString.isEmpty()) {
                            x = ((Player) sender).getLocation().getX();
                        } else {
                            double offset = Double.parseDouble(offsetString);
                            x = ((Player) sender).getLocation().getX() + offset;
                        }
                    } else {
                        x = Double.parseDouble(args[3]);
                    }
                    if (args[4].startsWith("~")) {
                        String offsetString = args[4].substring(1);
                        if (offsetString.isEmpty()) {
                            y = ((Player) sender).getLocation().getY();
                        } else {
                            double offset = Double.parseDouble(offsetString);
                            y = ((Player) sender).getLocation().getY() + offset;
                        }
                    } else {
                        y = Double.parseDouble(args[4]);
                    }

                    if (args[5].startsWith("~")) {
                        String offsetString = args[5].substring(1);
                        if (offsetString.isEmpty()) {
                            z = ((Player) sender).getLocation().getZ();
                        } else {
                            double offset = Double.parseDouble(offsetString);
                            z = ((Player) sender).getLocation().getZ() + offset;
                        }
                    } else {
                        z = Double.parseDouble(args[5]);
                    }
                    if (args.length >= 8){
                        //String typeAction = args[6];
                        String nameWorld = args[6];

                        final int index = 7;
                        if (getMarks(sender, args, nameWorld, index)) return true;
                    }
                    else {
                        sender.sendMessage(Component.text()
                                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                                .append(Component.text("Incorrect Syntax's", NamedTextColor.RED))
                                .build());
                    }
                }
                return true;

            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("Invalid coordinates provided.", NamedTextColor.RED))
                        .build());
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("action")){
            if (listener.worldConfig.getKeys(false).contains(args[2])){
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.YELLOW))
                        .append(Component.text("Registered", NamedTextColor.GREEN))
                        .build());
            }else {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("Event Not Found!", NamedTextColor.RED))
                        .build());
                return true;
            }
            return true;
        }

        Component errorMessage = Component.text()
                .append(Component.text("Command not recognized: ", NamedTextColor.RED))
                .append(Component.text(args[0], NamedTextColor.RED))
                .append(Component.text(". ", NamedTextColor.RED))
                .append(Component.text("Use ", NamedTextColor.RED))
                .append(Component.text("/"+ label + " help", NamedTextColor.BLUE))
                .append(Component.text(" for view details.", NamedTextColor.RED))
                .build();
        sender.sendMessage(errorMessage);
        return false;
    }

    private boolean getMarks(@NotNull CommandSender sender, String[] args, String nameWorld, int index) {
        int marks = Arrays.stream(args, index, args.length)
                .mapToInt(arg -> (int)arg.chars().filter(ch -> ch == '\"').count())
                .sum();


        StringBuilder builder = new StringBuilder();
        if (args[index].startsWith("\"") && marks == 2) {
            builder.append(Arrays.stream(args).skip(index).collect(Collectors.joining(" ")));
        }else {
            sender.sendMessage(Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                    .append(Component.text("Incorrect Syntax's in > \" ", NamedTextColor.RED))
                    .build());
            return true;
        }

        Pattern pattern = Pattern.compile("\"([^\"]*)\"\\s*(\\w+)?");
        Matcher matcher = pattern.matcher(builder.toString());

        StringBuilder commandBuilder = new StringBuilder();
        StringBuilder eventBuilder = new StringBuilder();
        while (matcher.find()) {
            commandBuilder.append(matcher.group(1));
            eventBuilder.append(matcher.group(2));
        }

        String command = commandBuilder.toString();
        String eventName = eventBuilder.toString();

        registerEvent(sender,eventName, nameWorld, command, x, y, z);
        return false;
    }


    private void registerEvent(CommandSender sender, String eventName, String nameWorld, String onCommand, double x, double y, double z) {

        World world = Bukkit.getWorld(nameWorld);
        if (world != null) {
            location = new Location(world, x, y, z);

            file = new File(plugin.getDataFolder(), "blocks_events/world.yml");
            worldConfig = YamlConfiguration.loadConfiguration(file);

            if (eventName.equals("null") || eventName.isEmpty()) {
                int counter = 1;
                while (worldConfig.contains("Event_" + counter)) {
                    counter++;
                }
                eventName = "Event_" + counter;
            }

            if (worldConfig.contains(eventName)) {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("This event already registered!", NamedTextColor.RED))
                        .build());
                return;
            }
            if (location.isChunkLoaded()){
                listener.locationCommands.put(eventName, new Object[]{location.getBlock(), onCommand, "", location.getChunk().getX(), location.getChunk().getZ(), world});
                sender.sendMessage(Component.text()
                        .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("Registered "+ eventName + ":", NamedTextColor.GREEN))
                        .append(Component.text("Is Loaded in memory.")));
                Component addMessage = Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_AQUA))
                        .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("Registered "+ eventName + ":", NamedTextColor.GREEN))
                        .append(Component.text(" X: "+location.getBlockX()+ " Y: "+location.getBlockY()+ " Z: "+location.getBlockZ() ,NamedTextColor.WHITE))
                        .append(Component.text(" [" + world.getName() + "] ", NamedTextColor.AQUA))
                        .build();
                Bukkit.getConsoleSender().sendMessage(addMessage);
            }else {
                sender.sendMessage(Component.text()
                        .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("Unregistered "+ eventName + ":", NamedTextColor.RED))
                        .append(Component.text("The chunk is not loaded but will be registered in memory when it is active.")));
                Component quitMessage = Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_AQUA))
                        .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text("Unregistered "+ eventName + ":", NamedTextColor.RED))
                        .append(Component.text(" X: "+location.getBlockX()+ " Y: "+location.getBlockY()+ " Z: "+location.getBlockZ() ,NamedTextColor.WHITE))
                        .append(Component.text(" [" + world.getName() + "] ", NamedTextColor.AQUA))
                        .build();
                Bukkit.getConsoleSender().sendMessage(quitMessage);
            }

            String finalEventName = eventName;

            Bukkit.getRegionScheduler().run(plugin, location, task -> {
                Block block = location.getBlock();
                worldConfig.set(finalEventName + ".world", nameWorld);
                worldConfig.set(finalEventName + ".x", x);
                worldConfig.set(finalEventName + ".y", y);
                worldConfig.set(finalEventName + ".z", z);
                worldConfig.set(finalEventName + ".blockType", block.getType().name());
                worldConfig.set(finalEventName + ".chunkX", location.getChunk().getX());
                worldConfig.set(finalEventName + ".chunkZ", location.getChunk().getZ());
                worldConfig.set(finalEventName + ".command", onCommand);
                config.saveWorldConfig(worldConfig, file);
                listener.worldConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "blocks_events/world" + ".yml"));
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.LIGHT_PURPLE))
                        .append(Component.text(" Success!", NamedTextColor.WHITE))
                        .append(Component.text(" event ", NamedTextColor.WHITE))
                        .append(Component.text(" ➜ ", NamedTextColor.AQUA))
                        .append(Component.text("[" + finalEventName + "] ", NamedTextColor.BLUE))
                        .append(Component.text("[" + nameWorld + "] ", NamedTextColor.DARK_AQUA))
                        .append(Component.text("[cords]", NamedTextColor.AQUA))
                        .append(Component.text(" [location block x y z] " + location.getBlockX() + " " + location.getBlockY() + " "+ location.getBlockZ(), NamedTextColor.GOLD))
                        .append(Component.text(" [type] " + block.getType(), NamedTextColor.WHITE))
                        .build());
            });
        }else {
            sender.sendMessage(Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                    .append(Component.text("nameWorld not found! ", NamedTextColor.WHITE))
                    .build());
        }
    }

    private void sendCommandList(CommandSender sender, String label) {
        Component commands = Component.text()
                .append(Component.text("[List of commands]", NamedTextColor.DARK_AQUA))
                .build();
        Component commands1 = Component.text()
                .append(Component.text("/"+ label, NamedTextColor.WHITE))
                .append(Component.text(" reload", NamedTextColor.BLUE))
                .append(Component.text("  - Reloads the configuration ", NamedTextColor.WHITE))
                .build();
        Component commands2 = Component.text()
                .append(Component.text("/"+ label, NamedTextColor.WHITE))
                .append(Component.text(" help", NamedTextColor.BLUE))
                .append(Component.text("  - View All Configurations of commands ", NamedTextColor.WHITE))
                .build();
        Component commands3 = Component.text()
                .append(Component.text("/"+ label, NamedTextColor.WHITE))
                .append(Component.text(" load <configName>", NamedTextColor.BLUE))
                .append(Component.text("  - Load a specific configuration file", NamedTextColor.WHITE))
                .build();
        sender.sendMessage(commands);
        sender.sendMessage(commands1);
        sender.sendMessage(commands2);
        sender.sendMessage(commands3);
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command tabCommand, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> availableCommands = Arrays.asList("load","reload","help", "register");

            List<String> matchingCommands = new ArrayList<>();
            for (String command : availableCommands) {
                if (command.startsWith(input)) {
                    matchingCommands.add(command);
                }
            }
            return matchingCommands;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("load")) {
            return getMatchingConfigNames(args[1]); //No Change this
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("register")) {
            if (args.length == 2) {
                return Arrays.asList("event","action");
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("event")) {
                return Collections.singletonList("block_in_destination");
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
                return getSuggestionsLocation(args[3],sender,args);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("action")){
                return new ArrayList<>(listener.worldConfig.getKeys(false));
            }
        }
        return new ArrayList<>();
    }

    private List<String> getSuggestionsLocation(String partialInput, CommandSender sender, String[] args) {
        List<String> suggestions = Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(playerName -> playerName.toLowerCase().startsWith(partialInput.toLowerCase()))
                .collect(Collectors.toList());

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location location = player.getLocation();
            x = location.getX();
            y = location.getY();
            z = location.getZ();

            suggestions.addAll(Arrays.asList(
                    String.format("%.2f", x),
                    String.format("%.2f %.2f", x, y),
                    String.format("%.2f %.2f %.2f", x, y, z),
                    "~",
                    "~ ~",
                    "~ ~ ~"
            ));

            String lastArg = args[args.length - 1];
            Player targetPlayer = Bukkit.getServer().getPlayer(args[3]);
            if (partialInput.isEmpty()) {
                return suggestions;
            } else if (targetPlayer != null) {
                if (args.length == 4) {
                    return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).filter(playerName -> playerName.toLowerCase().startsWith(partialInput.toLowerCase())).collect(Collectors.toList());
                }else if (args.length == 5) {
                    return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
                }else {
                    if (args[5].isEmpty())return Collections.singletonList("\"command\"");
                    int marks = Arrays.stream(args, 5, args.length)
                            .mapToInt(arg -> (int)arg.chars().filter(ch -> ch == '\"').count())
                            .sum();
                    if (marks > 2)return Collections.emptyList();
                }
            } else if (args.length == 4 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], y, z);
            } else if (args.length == 5 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], z);
            } else if (args.length == 6 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1]);
            }else if (args.length == 7){
                return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }else if (args.length >= 8){
                if (args[7].isEmpty())return Collections.singletonList("\"command\"");
                int marks = Arrays.stream(args, 6, args.length)
                        .mapToInt(arg -> (int)arg.chars().filter(ch -> ch == '\"').count())
                        .sum();
                if (marks > 2)return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private List<String> handleArgs(String tab, double... cords) {
        List<String> result = new ArrayList<>();
        try {
            double number = Double.parseDouble(tab);
            if (cords.length == 0) {
                result.add(String.format("%.2f", number));
            } else if (cords.length == 1) {
                result.addAll(Arrays.asList(
                        String.format("%.2f", number),
                        String.format("%.2f %.2f", number, cords[0])
                ));
            } else if (cords.length == 2) {
                result.addAll(Arrays.asList(
                        String.format("%.2f", number),
                        String.format("%.2f %.2f", number, cords[0]),
                        String.format("%.2f %.2f %.2f", number, cords[0], cords[1])
                ));
            }
        } catch (NumberFormatException ignored) {
            if (cords.length == 0) {
                result.add(tab);
            } else if (cords.length == 1) {
                result.addAll(Arrays.asList(
                        tab,
                        tab + " " + String.format("%.2f", cords[0])
                ));
            } else if (cords.length == 2) {
                result.addAll(Arrays.asList(
                        tab,
                        tab + " " + String.format("%.2f", cords[0]),
                        tab + " " + String.format("%.2f %.2f", cords[0], cords[1])
                ));
            }
        }
        return result;
    }
    private List<String> getMatchingConfigNames(String partialName) {
        List<String> matchingNames = new ArrayList<>();
        File profilesFolder = new File(plugin.getDataFolder(), "profiles");
        File[] files = profilesFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    String fileName = file.getName().replace(".yml", "");
                    if (fileName.toLowerCase().contains(partialName.toLowerCase())) {
                        matchingNames.add(fileName);
                    }
                }
            }
        }
        return matchingNames;
    }

    public String getConfigName() {
        return configName;
    }
}