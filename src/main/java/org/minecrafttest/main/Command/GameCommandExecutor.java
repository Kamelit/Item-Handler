package org.minecrafttest.main.Command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class GameCommandExecutor implements CommandExecutor, TabCompleter {
    private final ItemHandler commandItem;
    private final PlayerInteractionListener listener;
    private String configName = "";
    private Location destination;

    public GameCommandExecutor() {
        this.commandItem = ItemHandler.getPlugin();
        this.listener = commandItem.getListener();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            Component Message = Component.text()
                    .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.DARK_GRAY))
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
            commandItem.reloadConfig();
            String file = !Objects.equals(configName, "") ? configName : "subConfig";
            listener.updatePlayerInventories("profiles/" + file);

            Component enableMessage = Component.text()
                    .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.BLUE))
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
            File configFile = new File(commandItem.getDataFolder() + File.separator + "profiles", configName + ".yml");
            if (!configFile.exists()) {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("YML not Found ", NamedTextColor.RED))
                        .append(Component.text(configName + ".yml", NamedTextColor.RED))
                        .build());
                return true;
            }
            listener.updatePlayerInventories("profiles/" + configName);
            Component enableMessage = Component.text()
                    .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.BLUE))
                    .append(Component.text("Config loaded: " + configName, NamedTextColor.WHITE))
                    .build();
            sender.sendMessage(enableMessage);
            return true;
        }

        if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
            try {

                double x, y, z;
                Player targetPlayer = Bukkit.getPlayer(args[3]);
                if (targetPlayer != null) {
                    x = targetPlayer.getLocation().getX();
                    y = targetPlayer.getLocation().getY();
                    z = targetPlayer.getLocation().getZ();
                    destination = new Location(((Player) sender).getWorld(), x, y, z);
                }else {
                    if (args.length == 6) {
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
                        destination = new Location(((Player) sender).getWorld(), x, y, z);
                    }else {
                        sender.sendMessage(Component.text()
                                .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.RED))
                                .append(Component.text("Player not found or invalid coordinates", NamedTextColor.RED))
                                .build());
                    }
                }
                return true;

            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text()
                        .append(Component.text("[" + commandItem.getName() + "] ", NamedTextColor.RED))
                        .append(Component.text("Invalid coordinates provided.", NamedTextColor.RED))
                        .build());
                return true;
            }
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
        return true;
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
            List<String> availableCommands = Arrays.asList("load","reload","help","set", "register");

            List<String> matchingCommands = new ArrayList<>();
            for (String command : availableCommands) {
                if (command.startsWith(input)) {
                    matchingCommands.add(command);
                }
            }
            return matchingCommands;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("load")) {
            return getMatchingConfigNames(args[1]); //No Change this
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
            if (args.length == 2) {
                return Collections.singletonList("event");
            } else if (args.length == 3 && args[1].equalsIgnoreCase("event")) {
                return Collections.singletonList("block_in_destination");
            }else if (args.length >= 4 && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
                return getSuggestionsLocation(args[3],sender,args);
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
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();

            suggestions.addAll(Arrays.asList(
                    String.format("%.2f", x),
                    String.format("%.2f %.2f", x, y),
                    String.format("%.2f %.2f %.2f", x, y, z),
                    "~",
                    "~ ~",
                    "~ ~ ~"
            ));

            String lastArg = args[args.length - 1];
            Player targetPlayer = Bukkit.getServer().getPlayer(lastArg);
            if (partialInput.isEmpty()){
                return suggestions;
            }else if (targetPlayer != null && suggestions.contains(lastArg)) {
                return Collections.singletonList(lastArg);
            } else if (args.length == 4 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], y, z);
            } else if (args.length == 5 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], z);
            } else if (args.length == 6 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1]);
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
        File profilesFolder = new File(commandItem.getDataFolder(), "profiles");
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

    public Location getLocationFromCommand(){
        return destination;
    }

    public String getConfigName() {
        return configName;
    }
}