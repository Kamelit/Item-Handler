package org.minecrafttest.main.Command;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;
import org.minecrafttest.main.Particles.TypesAnimation;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;

import java.io.File;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//GameCommandExecutor
public class GameCommandExecutor implements CommandExecutor, TabCompleter {
    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final PlayerInteractionListener listener = plugin.getListener();
    private final Config config = plugin.getCustomConfig();
    private String configName = "";
    private Location location;
    private double x, y, z;
    private final List<String> typeActions = Arrays.asList("on_block_burned","on_click","on_right_click", "on_left_click",  "on_block_break",
            "on_block_place", "on_block_explode", "on_block_decay", "on_block_grow", "on_block_redstone_change", "on_block_state_player_change");
    private final List<String> playerItems = Arrays.asList(listener.groupKeysMenusType);
    private final File file = new File(plugin.getDataFolder(), "blocks_events/world.yml");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {


            MessageBuilder anotherMessageBuilder = MessageBuilder.createMessageBuilder();
            anotherMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_GRAY)
                    .append("Only players can Execute This Command ", ColorText.DARK_GRAY)
                    .build();
            anotherMessageBuilder.senderMessage(sender);
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 ) {
            MessageBuilder helpMessageBuilder = MessageBuilder.createMessageBuilder();
            helpMessageBuilder.append("Use ", ColorText.AQUA)
                    .append("/" + label + " help", ColorText.BLUE)
                    .append(" for view details.", ColorText.AQUA)
                    .build();
            helpMessageBuilder.senderMessage(sender);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendCommandList(sender, label);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("itemhandler.reload")){
            plugin.reloadConfig();
            String file = !Objects.equals(configName, "") ? configName : "subConfig";
            listener.updates("profiles/" + file);
                MessageBuilder enableMessageBuilder = MessageBuilder.createMessageBuilder();
                enableMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.BLUE)
                        .append("Reload Config! ", ColorText.WHITE)
                        .build();
                enableMessageBuilder.BukkitSender();
                enableMessageBuilder.senderMessage(sender);
            }else {
                MessageBuilder noPermissionMessageBuilder = MessageBuilder.createMessageBuilder();
                noPermissionMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                        .append("You do not have permission to use this command.", ColorText.RED)
                        .build();
                noPermissionMessageBuilder.senderMessage(sender);
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("Chronometer") && args[1].equalsIgnoreCase("start")){
            plugin.getChronometer().startChronometer(player, 5,0);
            return true;
        }

        if (args[0].equalsIgnoreCase("Chronometer") && args[1].equalsIgnoreCase("stop") ){
            //plugin.getParkour().RemoveCheckPointPlayer(player);
            plugin.getChronometer().stopChronometer(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("Chronometer") && args[1].equalsIgnoreCase("restart") ){
            if (plugin.getChronometer().isRunChronometer(player)) plugin.getChronometer().stopChronometer(player);
            plugin.getChronometer().startChronometer(player, 5,0);
            //plugin.getParkour().RemoveCheckPointPlayer(player);
            //plugin.getParkour().getPlayerLastCheckpoint().remove(player);
            ConfigurationSection configurationSection = listener.worldConfig.getConfigurationSection("Parkour");
            if (configurationSection != null){
                int x = configurationSection.getInt("x");
                int y = configurationSection.getInt("y");
                int z = configurationSection.getInt("z");
                String name = configurationSection.getString("world");
                World world = Bukkit.getWorld(Objects.requireNonNull(name));
                float yaw = player.getLocation().getYaw();
                float pitch = player.getLocation().getPitch();
                Location location = new Location(world,x + 0.5 ,y + 0.5, z + 0.5, yaw, pitch);
                player.teleportAsync(location);
            }
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("load")) {
            if (args.length >= 3 && args[1].equalsIgnoreCase("only_me")) {
                if (player.hasPermission("itemhandler.load.only_me")) {
                    if (playerItems.contains(args[2].toLowerCase())) {
                        if (!listener.playerInventory.get(args[2]).contains(player)) {
                            listener.playerInventory.values().forEach(players -> players.remove(player));
                            player.getInventory().clear();
                            listener.setItems(player, args[2].toLowerCase());
                            listener.playerInventory.get(args[2]).add(player);
                            listener.runThreads();
                        }
                    } else {
                        MessageBuilder notFoundMessageBuilder = MessageBuilder.createMessageBuilder();
                        notFoundMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                .append("Items of \"" + args[2] + "\" Not Found", ColorText.RED)
                                .build();
                        notFoundMessageBuilder.senderMessage(sender);
                    }
                } else {
                    MessageBuilder noPermissionMessageBuilder = MessageBuilder.createMessageBuilder();
                    noPermissionMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("You do not have permission to use this command.", ColorText.RED)
                            .build();
                    noPermissionMessageBuilder.senderMessage(sender);
                }
                return true;
            }

            if (player.hasPermission("itemhandler.load")) {
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
                    MessageBuilder ymlNotFoundMessageBuilder = MessageBuilder.createMessageBuilder();
                    ymlNotFoundMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("YML not Found ", ColorText.RED)
                            .append(configName + ".yml", ColorText.RED)
                            .build();
                    ymlNotFoundMessageBuilder.senderMessage(sender);
                    return true;
                }
                listener.updates("profiles/" + configName);
                MessageBuilder enableMessageBuilder = MessageBuilder.createMessageBuilder();
                enableMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.BLUE)
                        .append("Config loaded: " + configName, ColorText.WHITE)
                        .build();
                enableMessageBuilder.senderMessage(sender);
            } else {
                MessageBuilder noPermissionMessageBuilder = MessageBuilder.createMessageBuilder();
                noPermissionMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                        .append("You do not have permission to use this command.", ColorText.RED)
                        .build();
                noPermissionMessageBuilder.senderMessage(sender);
            }
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
            try {
                Player targetPlayer = Bukkit.getPlayer(args[3]);
                if (targetPlayer != null) {
                    x = targetPlayer.getLocation().getBlockX() + 0.5;
                    y = targetPlayer.getLocation().getBlockY() + 0.5;
                    z = targetPlayer.getLocation().getBlockZ() + 0.5;
                    if (args.length >= 6){
                        String nameWorld = args[4];
                        final int index = 5;
                        if (registerEventByCommand(sender, args, nameWorld, index)) return true;
                    }else {
                        MessageBuilder incorrectSyntaxMessageBuilder = MessageBuilder.createMessageBuilder();
                        incorrectSyntaxMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                .append("Incorrect Syntax's", ColorText.RED)
                                .build();
                        incorrectSyntaxMessageBuilder.senderMessage(sender);
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
                        if (registerEventByCommand(sender, args, nameWorld, index)) return true;
                    }
                    else {
                        MessageBuilder syntaxErrorMessageBuilder = MessageBuilder.createMessageBuilder();
                        syntaxErrorMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                .append("Incorrect Syntax's", ColorText.RED)
                                .build();
                        syntaxErrorMessageBuilder.senderMessage(sender);
                    }
                }
                return true;

            } catch (NumberFormatException e) {
                MessageBuilder invalidCoordinatesMessageBuilder = MessageBuilder.createMessageBuilder();
                invalidCoordinatesMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                        .append("Invalid coordinates provided.", ColorText.RED)
                        .build();
                invalidCoordinatesMessageBuilder.senderMessage(sender);
                return true;
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("type_action")){
            if (args.length > 2){
                if (listener.worldConfig.getKeys(false).contains(args[2])){

                    List<String> typeActionsLowerCase = typeActions.stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());

                    Set<String> seen = new HashSet<>();

                    List<String> actions = Arrays.stream(args, 3, args.length)
                            .map(String::toLowerCase)
                            .filter(action -> {
                                if (typeActionsLowerCase.contains(action)) {
                                    return seen.add(action);
                                } else {
                                    MessageBuilder actionNotRecognizableMessageBuilder = MessageBuilder.createMessageBuilder();
                                    actionNotRecognizableMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                            .append("action '" + action + "' Is not recognizable.", ColorText.RED)
                                            .build();
                                    actionNotRecognizableMessageBuilder.senderMessage(sender);
                                    return false;
                                }
                            })
                            .collect(Collectors.toList());

                    listener.worldConfig.set(args[2]+".action", actions);
                    config.saveWorldConfig(listener.worldConfig, file);


                    if (!actions.isEmpty()) {
                        if (listener.worldConfigInMemory.containsKey(args[2])) {
                            List<Object> all = listener.worldConfigInMemory.get(args[2]);
                            List<Object> clonedList = new ArrayList<>(all);
                            if (clonedList.size() >= 7) clonedList.subList(6, clonedList.size()).clear();
                            clonedList.addAll(actions);
                            listener.worldConfigInMemory.replace(args[2], clonedList);
                        }
                        MessageBuilder registeredActionsMessageBuilder = MessageBuilder.createMessageBuilder();
                        registeredActionsMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.GREEN)
                                .append("Registered!: " + String.join(", ", actions), ColorText.GREEN)
                                .build();
                        registeredActionsMessageBuilder.senderMessage(sender);
                    }
                }else {
                    MessageBuilder eventNotFoundMessageBuilder = MessageBuilder.createMessageBuilder();
                    eventNotFoundMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("Event Not Found!", ColorText.RED)
                            .build();
                    eventNotFoundMessageBuilder.senderMessage(sender);

                    return true;
                }
            }
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("animation")){
            if (args.length > 2 && listener.worldConfig.getKeys(false).contains(args[2])) {
                if (args.length == 3){
                    MessageBuilder selectAnimationMessageBuilder = MessageBuilder.createMessageBuilder();
                    selectAnimationMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("Select Animation", ColorText.RED)
                            .build();
                    selectAnimationMessageBuilder.senderMessage(sender);
                    return true;
                }
                TypesAnimation animation = getAnimationByName(args[3]);
                int range_of_view = 0;
                if (args.length > 4)
                    try {
                    range_of_view = Integer.parseInt(args[4]);
                    } catch (Exception e){
                        MessageBuilder invalidCastMessageBuilder = MessageBuilder.createMessageBuilder();
                        invalidCastMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                .append("Invalid " + args[4] + " to cast to int ", ColorText.RED)
                                .build();
                        invalidCastMessageBuilder.senderMessage(sender);
                        return true;
                }

                if (animation != null) {
                    listener.worldConfig.set(args[2] + ".animation", Arrays.asList(animation.name(),range_of_view));
                    config.saveWorldConfig(listener.worldConfig, file);

                    if (listener.worldConfigInMemory.containsKey(args[2])) {
                        List<Object> metadata = new ArrayList<>(listener.worldConfigInMemory.get(args[2]));
                        List<?> descAnimation = Arrays.asList(animation.name(), range_of_view); //new ArrayList
                        List<?> metadataSubList = (List<?>) metadata.get(5);
                            if (!metadataSubList.equals(descAnimation)) {
                                plugin.getParticleAnimation().RemoveTaskingParticles(args[2]);
                                metadata.set(5, descAnimation);
                                plugin.getParticleAnimation().playAnimation(args[2], (Block) metadata.get(0), animation, range_of_view);
                                listener.worldConfigInMemory.replace(args[2], metadata);
                                MessageBuilder registeredAnimationMessageBuilder = MessageBuilder.createMessageBuilder();
                                registeredAnimationMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.GREEN)
                                        .append("Registered Animation!: " + String.join(", ", String.valueOf(descAnimation)), ColorText.GREEN)
                                        .build();
                                registeredAnimationMessageBuilder.senderMessage(sender);

                            }
                    }
                }
            }else {
                MessageBuilder eventNotFoundMessageBuilder = MessageBuilder.createMessageBuilder();
                eventNotFoundMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                        .append("Event Not Found!", ColorText.RED)
                        .build();
                eventNotFoundMessageBuilder.senderMessage(sender);

            }
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("new_command_in_event")){
            if (args.length > 2 && listener.worldConfig.getKeys(false).contains(args[2])) {
                int index = 3;
                String input = Arrays.stream(args, index, args.length).collect(Collectors.joining(" "));

                int marks = Arrays.stream(args, index, args.length)
                        .mapToInt(arg -> (int)arg.chars().filter(ch -> ch == '\"').count())
                        .sum();

                Pattern pattern = Pattern.compile("\"([^\"]*)\"");
                Matcher matcher = pattern.matcher(input);

                if (matcher.find() && marks == 2) {
                    String command = matcher.group(1);
                    List<String> commands = listener.worldConfig.getStringList(args[2]+".command");
                    commands.add(command);
                    listener.worldConfig.set(args[2]+".command", commands);
                    config.saveWorldConfig(listener.worldConfig, file);

                    if (listener.worldConfigInMemory.containsKey(args[2])) {
                        List<Object> metadata = new ArrayList<>(listener.worldConfigInMemory.get(args[2]));
                        metadata.set(1, commands);
                        listener.worldConfigInMemory.replace(args[2], metadata);
                        MessageBuilder registeredCommandsMessageBuilder = MessageBuilder.createMessageBuilder();
                        registeredCommandsMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.GREEN)
                                .append("Registered Commands!: " + String.join(", ", String.valueOf(commands)), ColorText.GREEN)
                                .build();
                        registeredCommandsMessageBuilder.senderMessage(sender);
                    }

                } else {
                    MessageBuilder incorrectSyntaxMessageBuilder = MessageBuilder.createMessageBuilder();
                    incorrectSyntaxMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("Incorrect Syntax in > \" ", ColorText.RED)
                            .build();
                    incorrectSyntaxMessageBuilder.senderMessage(sender);
                }
                return true;
            }
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("name")){
            if (plugin.getParkour().RegisterParkourName(args[3])){
                sender.sendMessage("ok");
            }else {
                sender.sendMessage("nope");
            }
            return true;
        }

        if (args.length > 3 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("all_minimums_y") || args[2].equalsIgnoreCase("all_maximum_y")){
            if (args.length > 4 ){
                try {
                    int index = 4   ;
                    if (args[index].startsWith("~")) {
                        String offsetString = args[index].substring(1);
                        if (offsetString.isEmpty()) {
                            y = ((Player) sender).getLocation().getY();
                        } else {
                            double offset = Double.parseDouble(offsetString);
                            y = ((Player) sender).getLocation().getY() + offset;
                        }
                    } else {
                        y = Double.parseDouble(args[index]);
                    }
                    String type = args[2].equalsIgnoreCase("all_minimums_y")? "min_y":"max_y";
                    plugin.getParkour().RegisterParkourAllFallInY(args[3], (int) y, type, sender);
                    return true;
                }catch (NumberFormatException e) {
                    MessageBuilder invalidCoordinatesMessageBuilder = MessageBuilder.createMessageBuilder();
                    invalidCoordinatesMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("Invalid coordinates provided.", ColorText.RED)
                            .build();
                    invalidCoordinatesMessageBuilder.senderMessage(sender);
                }
            }
        }

        if (args.length >= 4 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("minimums_y") || args[2].equalsIgnoreCase("maximum_y")){
            if (args.length > 4 && plugin.getParkour().parkourConfiguration.contains(args[3])){
                ConfigurationSection parkourSection = plugin.getParkour().parkourConfiguration.getConfigurationSection(args[3]);
                if (args.length > 5 && parkourSection != null){
                    if (args.length == 6){
                        if (Objects.requireNonNull(parkourSection).contains(args[4])){
                            try {
                                int index = 5   ;
                                if (args[index].startsWith("~")) {
                                    String offsetString = args[index].substring(1);
                                    if (offsetString.isEmpty()) {
                                        y = ((Player) sender).getLocation().getY();
                                    } else {
                                        double offset = Double.parseDouble(offsetString);
                                        y = ((Player) sender).getLocation().getY() + offset;
                                    }
                                } else {
                                    y = Double.parseDouble(args[index]);
                                }

                                boolean isMin = args[2].equalsIgnoreCase("minimums_y");
                                String type = isMin? "min_y":"max_y";
                                if (plugin.getParkour().RegisterParkourFallInY(args[3], args[4], (int) y, type ,isMin)){
                                    sender.sendMessage("Registered "+ type);
                                }else {
                                    sender.sendMessage("La posicion es mayor o menor que el checkpoint");
                                }

                            }catch (NumberFormatException e) {
                                MessageBuilder invalidCoordinatesMessageBuilder = MessageBuilder.createMessageBuilder();
                                invalidCoordinatesMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                        .append("Invalid coordinates provided.", ColorText.RED)
                                        .build();
                                invalidCoordinatesMessageBuilder.senderMessage(sender);
                            }
                        }
                    }
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("parkour") && args[1].equalsIgnoreCase("start")) {
            String parkourName = args[2];
            if (plugin.getParkour().parkourConfiguration.contains(parkourName)) {
                plugin.getParkour().setPlayerParkour(player, parkourName);
                plugin.getParkour().loadAnimationChecks(player);
                //sender.sendMessage("Parkour " + parkourName + " Start.");
            } else {
                sender.sendMessage(parkourName + " no exist.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("parkour") && args[1].equalsIgnoreCase("restart")) {
            String parkourName = args[2];
            if (plugin.getParkour().parkourConfiguration.contains(args[2])) {
                plugin.getParkour().RemoveCheckPointPlayer(args[2], player);
                plugin.getParkour().removePlayerParkour(player, args[2]);
                plugin.getParkour().getPlayerLastCheckpoint().remove(player);
                plugin.getParkour().setPlayerParkour(player, args[2]);
                plugin.getParkour().loadAnimationChecks(player);
                plugin.getScore().removePlayerScore(player);

                //sender.sendMessage("Parkour " + parkourName + " Restart.");
            } else {
                sender.sendMessage(parkourName + " no exist.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("parkour") && args[1].equalsIgnoreCase("finish")) {
            String parkourName = args[2];
            if (plugin.getParkour().parkourConfiguration.contains(args[2])) {
                plugin.getParkour().getPlayerLastCheckpoint().remove(player);
                plugin.getParkour().RemoveCheckPointPlayer(parkourName, player);
                plugin.getParkour().removePlayerParkour(player, parkourName);
                plugin.getScore().removePlayerScore(player);

                //sender.sendMessage("Parkour " + parkourName + " Fin.");
            } else {
                sender.sendMessage(parkourName + " no exist.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("parkour") && args[1].equalsIgnoreCase("teleport_last_checkpoint")){
            player.teleportAsync(plugin.getParkour().GetCheckpointInParkour());
        }

        if (args.length > 3 && args[0].equalsIgnoreCase("register") && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("checkpoint")){
            if (args.length > 4 && plugin.getParkour().parkourConfiguration.contains(args[3])){
                int index = 4;
                try {
                    Player targetPlayer = Bukkit.getPlayer(args[index]);
                    if (targetPlayer != null) {

                        if (args.length >= index+2){
                            String nameWorld = args[index+1];

                            if (plugin.getParkour().RegisterParkour(args[3],nameWorld, targetPlayer.getLocation().getBlockX(), targetPlayer.getLocation().getBlockY(), targetPlayer.getLocation().getBlockZ() )){
                                sender.sendMessage("Registered Parkour Checkpoint");
                            }else {
                                sender.sendMessage("No Registered");
                            }

                        }else {
                            MessageBuilder incorrectSyntaxMessageBuilder = MessageBuilder.createMessageBuilder();
                            incorrectSyntaxMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                    .append("Incorrect Syntax's", ColorText.RED)
                                    .build();
                            incorrectSyntaxMessageBuilder.senderMessage(sender);
                        }
                    }else {
                        if (args[index].startsWith("~")) {
                            String offsetString = args[index].substring(1);
                            if (offsetString.isEmpty()) {
                                x = ((Player) sender).getLocation().getX();
                            } else {
                                double offset = Double.parseDouble(offsetString);
                                x = ((Player) sender).getLocation().getX() + offset;
                            }
                        } else {
                            x = Double.parseDouble(args[index]);
                        }
                        if (args[index+1].startsWith("~")) {
                            String offsetString = args[index+1].substring(1);
                            if (offsetString.isEmpty()) {
                                y = ((Player) sender).getLocation().getY();
                            } else {
                                double offset = Double.parseDouble(offsetString);
                                y = ((Player) sender).getLocation().getY() + offset;
                            }
                        } else {
                            y = Double.parseDouble(args[index+1]);
                        }

                        if (args[index+2].startsWith("~")) {
                            String offsetString = args[index+2].substring(1);
                            if (offsetString.isEmpty()) {
                                z = ((Player) sender).getLocation().getZ();
                            } else {
                                double offset = Double.parseDouble(offsetString);
                                z = ((Player) sender).getLocation().getZ() + offset;
                            }
                        } else {
                            z = Double.parseDouble(args[index+2]);
                        }
                        if (args.length >= index+4){
                            //String typeAction = args[6];
                            String nameWorld = args[index+3];

                            if (plugin.getParkour().RegisterParkour(args[3],nameWorld, (int) x , (int) y, (int) z)){
                                sender.sendMessage("Registered Parkour Checkpoint");
                            }else {
                                sender.sendMessage("No Registered");
                            }
                        }
                        else {
                            MessageBuilder incorrectSyntaxMessageBuilder = MessageBuilder.createMessageBuilder();
                            incorrectSyntaxMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                                    .append("Incorrect Syntax's", ColorText.RED)
                                    .build();
                            incorrectSyntaxMessageBuilder.senderMessage(sender);
                        }
                    }
                    return true;

                } catch (NumberFormatException e) {
                    MessageBuilder invalidCoordinatesMessageBuilder = MessageBuilder.createMessageBuilder();
                    invalidCoordinatesMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                            .append("Invalid coordinates provided.", ColorText.RED)
                            .build();
                    invalidCoordinatesMessageBuilder.senderMessage(sender);
                    return true;
                }
            }else {
                sender.sendMessage("Error");
                return true;
            }
        }

        MessageBuilder errorMessageBuilder = MessageBuilder.createMessageBuilder();
        errorMessageBuilder.append("Command not recognized: ", ColorText.RED)
                .append(args[0], ColorText.RED)
                .append(". ", ColorText.RED)
                .append("Use ", ColorText.RED)
                .append("/" + label + " help", ColorText.BLUE)
                .append(" for view details.", ColorText.RED)
                .build();
        errorMessageBuilder.senderMessage(sender);
        return false;
    }


    private boolean registerEventByCommand(@NotNull CommandSender sender, String[] args, String nameWorld, int index) {
        int marks = Arrays.stream(args, index, args.length)
                .mapToInt(arg -> (int)arg.chars().filter(ch -> ch == '\"').count())
                .sum();


        StringBuilder builder = new StringBuilder();
        if (args[index].startsWith("\"") && marks == 2) {
            builder.append(Arrays.stream(args).skip(index).collect(Collectors.joining(" ")));
        }else {
            MessageBuilder syntaxErrorMessageBuilder = MessageBuilder.createMessageBuilder();
            syntaxErrorMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                    .append("Incorrect Syntax's in > \" ", ColorText.RED)
                    .build();
            syntaxErrorMessageBuilder.senderMessage(sender);

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


            if (eventName.equals("null") || eventName.isEmpty()) {
                int counter = 1;
                while (listener.worldConfig.contains("Event_" + counter)) {
                    counter++;
                }
                eventName = "Event_" + counter;
            }

            if (listener.worldConfig.contains(eventName)) {
                MessageBuilder eventAlreadyRegisteredMessageBuilder = MessageBuilder.createMessageBuilder();
                eventAlreadyRegisteredMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                        .append("This event already registered!", ColorText.RED)
                        .build();
                eventAlreadyRegisteredMessageBuilder.senderMessage(sender);
                return;
            }
            if (location.isChunkLoaded()){
                listener.worldConfigInMemory.put(eventName, Arrays.asList(location.getBlock(), Collections.singletonList(onCommand), location.getChunk().getX(), location.getChunk().getZ(), world, Arrays.asList(TypesAnimation.NONE.name(),0), ""));
                // Mensaje para el sender
                MessageBuilder registeredEventMessageBuilder = MessageBuilder.createMessageBuilder();
                registeredEventMessageBuilder.append("[OptimizerHandler] ", ColorText.LIGHT_PURPLE)
                        .append("Registered " + eventName + ":", ColorText.GREEN)
                        .append("Is Loaded in memory.")
                        .build();
                registeredEventMessageBuilder.senderMessage(sender);

                // Mensaje para el console sender
                MessageBuilder locationMessageBuilder = MessageBuilder.createMessageBuilder();
                locationMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                        .append("[OptimizerHandler] ", ColorText.LIGHT_PURPLE)
                        .append("Registered " + eventName + ":", ColorText.GREEN)
                        .append(" X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(), ColorText.WHITE)
                        .append(" [" + world.getName() + "] ", ColorText.AQUA)
                        .build();
                locationMessageBuilder.BukkitSender();

            }else {
                // Mensaje para el sender
                MessageBuilder unregisteredEventMessageBuilder = MessageBuilder.createMessageBuilder();
                unregisteredEventMessageBuilder.append("[OptimizerHandler] ", ColorText.LIGHT_PURPLE)
                        .append("Unregistered " + eventName + ":", ColorText.RED)
                        .append("The chunk is not loaded but will be registered in memory when it is active.")
                        .build();
                unregisteredEventMessageBuilder.senderMessage(sender);

                // Mensaje para el console sender
                MessageBuilder quitMessageBuilder = MessageBuilder.createMessageBuilder();
                quitMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_AQUA)
                        .append("[OptimizerHandler] ", ColorText.LIGHT_PURPLE)
                        .append("Unregistered " + eventName + ":", ColorText.RED)
                        .append(" X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(), ColorText.WHITE)
                        .append(" [" + world.getName() + "] ", ColorText.AQUA)
                        .build();
                quitMessageBuilder.BukkitSender();
            }

            String finalEventName = eventName;

            Bukkit.getRegionScheduler().run(plugin, location, task -> {
                Block block = location.getBlock();
                listener.worldConfig.set(finalEventName + ".world", nameWorld);
                listener.worldConfig.set(finalEventName + ".x", x);
                listener.worldConfig.set(finalEventName + ".y", y);
                listener.worldConfig.set(finalEventName + ".z", z);
                listener.worldConfig.set(finalEventName + ".blockType", block.getType().name());
                listener.worldConfig.set(finalEventName + ".chunkX", location.getChunk().getX());
                listener.worldConfig.set(finalEventName + ".chunkZ", location.getChunk().getZ());
                listener.worldConfig.set(finalEventName + ".command", Collections.singletonList(onCommand));
                listener.worldConfig.set(finalEventName + ".animation", Arrays.asList(TypesAnimation.NONE.name(),0));
                listener.worldConfig.set(finalEventName + ".action", "");
                config.saveWorldConfig(listener.worldConfig, file);
                MessageBuilder successEventMessageBuilder = MessageBuilder.createMessageBuilder();
                successEventMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.LIGHT_PURPLE)
                        .append(" Success!", ColorText.WHITE)
                        .append(" event ", ColorText.WHITE)
                        .append(" ➜ ", ColorText.AQUA)
                        .append("[" + finalEventName + "] ", ColorText.BLUE)
                        .append("[" + nameWorld + "] ", ColorText.DARK_AQUA)
                        .append("[cords]", ColorText.AQUA)
                        .append(" [location block x y z] " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ(), ColorText.GOLD)
                        .append(" [type] " + block.getType(), ColorText.WHITE)
                        .build();
                successEventMessageBuilder.senderMessage(sender);
            });
        }else {
            MessageBuilder nameWorldNotFoundMessageBuilder = MessageBuilder.createMessageBuilder();
            nameWorldNotFoundMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.RED)
                    .append("nameWorld not found! ", ColorText.WHITE)
                    .build();
            nameWorldNotFoundMessageBuilder.senderMessage(sender);

        }
    }

    private void sendCommandList(CommandSender sender, String label) {
        // Mensaje de lista de comandos
        MessageBuilder commandsMessageBuilder = MessageBuilder.createMessageBuilder();
        commandsMessageBuilder.append("[List of commands]", ColorText.DARK_AQUA)
                .build();
        commandsMessageBuilder.senderMessage(sender);

        // Comando /label reload
        MessageBuilder reloadCommandBuilder = MessageBuilder.createMessageBuilder();
        reloadCommandBuilder.append("/" + label, ColorText.WHITE)
                .append(" reload", ColorText.BLUE)
                .append("  - Reloads the configuration ", ColorText.WHITE)
                .build();
        reloadCommandBuilder.senderMessage(sender);

        // Comando /label help
        MessageBuilder helpCommandBuilder = MessageBuilder.createMessageBuilder();
        helpCommandBuilder.append("/" + label, ColorText.WHITE)
                .append(" help", ColorText.BLUE)
                .append("  - View All Configurations of commands ", ColorText.WHITE)
                .build();
        helpCommandBuilder.senderMessage(sender);

        // Comando /label load <configName>
        MessageBuilder loadCommandBuilder = MessageBuilder.createMessageBuilder();
        loadCommandBuilder.append("/" + label, ColorText.WHITE)
                .append(" load <configName>", ColorText.BLUE)
                .append("  - Load a specific configuration file", ColorText.WHITE)
                .build();
        loadCommandBuilder.senderMessage(sender);
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
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("load")) {
            if (args.length == 3) return playerItems;
            List<String> files = getMatchingConfigNames(args[1]);//No Change this
            if (!files.contains("only_me")) files.add("only_me");
            return files;
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("register")) {
            if (args.length == 2) {
                return Arrays.asList("event","type_action","animation", "new_command_in_event", "parkour");
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("new_command_in_event")) {
                return new ArrayList<>(listener.worldConfig.getKeys(false));
            }if (args.length >= 4 && args[1].equalsIgnoreCase("new_command_in_event") && listener.worldConfig.contains(args[2])){
                if (args[3].isEmpty())return Collections.singletonList("\"command\"");
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("event")) {
                return Collections.singletonList("block_in_destination");
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("event") && args[2].equalsIgnoreCase("block_in_destination")) {
                return getSuggestionsLocation(args[3],sender,args);
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("type_action")){
                return new ArrayList<>(listener.worldConfig.getKeys(false));
            }
            if (args.length >= 4 && args[1].equalsIgnoreCase("type_action") && listener.worldConfig.contains(args[2])) {
                return typeActions.stream()
                        .filter(action -> !Arrays.asList(args).contains(action))
                        .collect(Collectors.toList());
            }
            if (args.length == 3 && args[1].equalsIgnoreCase("animation")){
                return new ArrayList<>(listener.worldConfig.getKeys(false));
            }
            if (args.length == 4 && args[1].equalsIgnoreCase("animation") && listener.worldConfig.contains(args[2])) {
                return Arrays.stream(TypesAnimation.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());
            }

            if (args.length == 3 && args[1].equalsIgnoreCase("parkour") ){
                return Arrays.asList("checkpoint", "name", "minimums_y", "maximum_y", "all_minimums_y", "all_maximum_y");
            }

            if (args.length == 4 && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("name")){
                int c = 1;
                String name = "Parkour_";
                while (plugin.getParkour().parkourConfiguration.contains(name+c)){
                    c++;
                }
                name = name+c;
                return Collections.singletonList(name);
            }


            if (args.length == 4 && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("checkpoint")){
                return new ArrayList<>(plugin.getParkour().parkourConfiguration.getKeys(false));
            }

            if (args.length > 3 && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("checkpoint") && plugin.getParkour().parkourConfiguration.contains(args[3])){
                int index = 5;
                return cordsParkour(args[4],sender,args,index);
            }

            if (args.length >= 4 && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("minimums_y") ||  args[2].equalsIgnoreCase("maximum_y")){

                if (args.length == 4){
                    return new ArrayList<>(plugin.getParkour().parkourConfiguration.getKeys(false));
                }
                if (plugin.getParkour().parkourConfiguration.contains(args[3]) && args.length == 5){
                    ConfigurationSection parkourSection = plugin.getParkour().parkourConfiguration.getConfigurationSection(args[3]);
                    if (parkourSection != null) {
                        return new ArrayList<>(parkourSection.getKeys(false));
                    }
                }
                if (args.length == 6){
                    ConfigurationSection parkourSection = plugin.getParkour().parkourConfiguration.getConfigurationSection(args[3]);
                    if (Objects.requireNonNull(parkourSection).contains(args[4])){
                        Player player = (Player) sender;
                        return Arrays.asList(String.valueOf(player.getLocation().getY()), "~", String.valueOf(parkourSection.getInt("y")));
                    }
                }
            }

            if (args.length >= 4 && args[1].equalsIgnoreCase("parkour") && args[2].equalsIgnoreCase("all_minimums_y") || args[2].equalsIgnoreCase("all_maximum_y")){

                if (args.length == 4){
                    return new ArrayList<>(plugin.getParkour().parkourConfiguration.getKeys(false));
                }
                if (plugin.getParkour().parkourConfiguration.contains(args[3]) && args.length == 5){
                    ConfigurationSection parkourSection = plugin.getParkour().parkourConfiguration.getConfigurationSection(args[3]);
                    if (Objects.requireNonNull(parkourSection).contains(args[4])){
                        Player player = (Player) sender;
                        return Arrays.asList(String.valueOf(player.getLocation().getY()), "~");
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private List<String> cordsParkour(String partialInput, CommandSender sender, String[] args, int index){
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
            Player targetPlayer = Bukkit.getServer().getPlayer(args[index-1]);
            if (partialInput.isEmpty()) {
                return suggestions;
            } else if (targetPlayer != null) {
                if (args.length == index) {
                    return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).filter(playerName -> playerName.toLowerCase().startsWith(partialInput.toLowerCase())).collect(Collectors.toList());
                }else if (args.length == index+1) {
                    return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
                }
            } else if (args.length == index && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], y, z);
            } else if (args.length == index+1 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1], z);
            } else if (args.length == index+2 && lastArg.matches("^(-?\\d*\\.?\\d+|~)$")) {
                return handleArgs(args[args.length - 1]);
            }else if (args.length == index+3){
                return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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

    private TypesAnimation getAnimationByName(String name) {
        for (TypesAnimation animation : TypesAnimation.values()) {
            if (animation.name().equalsIgnoreCase(name)) {
                return animation;
            }
        }
        return null;
    }

    public String getConfigName() {
        return configName;
    }
}