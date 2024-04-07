package org.minecrafttest.main;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecrafttest.main.Command.GameCommandExecutor;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.Listener.PlayerInteractionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//ItemHandler
public class ItemHandler extends JavaPlugin {
    private static ItemHandler instance;
    private Config pluginConfig;
    private PlayerInteractionListener listener;
    private GameCommandExecutor executor;
    @Override
    public void onEnable() {
        instance = this;
        pluginConfig = new Config();
        pluginConfig.loadConfig();
        listener = new PlayerInteractionListener();
        executor = new GameCommandExecutor();
        Bukkit.getServer().getPluginManager().registerEvents(listener, this);
        List<String> aliases = new ArrayList<>();
        aliases.add("ih");
        PluginCommand command = Objects.requireNonNull(getCommand("ItemHandler"));
        command.setAliases(aliases);
        command.setExecutor(executor);
        listener.loadAllResources();
        listener.runThreads();
        Component enableMessage = Component.text()
                .append(Component.text("[" + this.getName() + "] ", NamedTextColor.GREEN))
                .append(Component.text("Load: " , NamedTextColor.WHITE))
                .append(Component.text("Ok " , NamedTextColor.GREEN))
                .build();
        Bukkit.getConsoleSender().sendMessage(enableMessage);
    }
    @Override
    public void onDisable() {
        listener.cancelMaterialChangeTasks(true);
        Component enableMessage = Component.text()
                .append(Component.text("[" + this.getName() + "] ", NamedTextColor.BLUE))
                .append(Component.text("Close " + this.getName() , NamedTextColor.BLUE))
                .build();
        Bukkit.getConsoleSender().sendMessage(enableMessage);
    }
    public Config getCustomConfig(){
        return pluginConfig;
    }
    public PlayerInteractionListener getListener(){
        return listener;
    }
    public GameCommandExecutor getExecutor(){
        return executor;
    }
    public static ItemHandler getPlugin() {
        return instance;
    }
}