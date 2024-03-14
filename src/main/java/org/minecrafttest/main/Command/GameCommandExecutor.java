package org.minecrafttest.main.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.minecrafttest.main.Listener.PlayerInteractionListener;
import org.minecrafttest.main.Main;

public class MainCommand implements CommandExecutor {

    private final Main plugin;
    private final PlayerInteractionListener mainActions;
    boolean clearInventory;
    boolean changeHand;
    public MainCommand(Main plugin, PlayerInteractionListener mainActions, boolean clearInventory, boolean changeHand) {
        this.plugin = plugin;
        this.mainActions = mainActions;
        this.clearInventory = clearInventory;
        this.changeHand = changeHand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("reloadConfig")) {
            plugin.reloadConfig();
            FileConfiguration config = plugin.getConfig();
            clearInventory = config.getBoolean("clear_inventory");
            changeHand = config.getBoolean("change_hand");

            mainActions.updatePlayerInventories();

            sender.sendMessage("Config Reload!.");
            return true;
        }
        return false;
    }
}