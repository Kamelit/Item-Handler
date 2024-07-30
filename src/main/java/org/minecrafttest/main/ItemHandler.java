package org.minecrafttest.main;


import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.minecrafttest.main.Command.GameCommandExecutor;
import org.minecrafttest.main.Command.HologramCommand;
import org.minecrafttest.main.Config.Config;
import org.minecrafttest.main.Database.Database;
import org.minecrafttest.main.Hologram.HoloListener;
import org.minecrafttest.main.Hologram.Hologram;
import org.minecrafttest.main.Listener.PlayerInteractionListener;
import org.minecrafttest.main.Parkour.Parkour;
import org.minecrafttest.main.Parkour.ParkourListener;
import org.minecrafttest.main.Parkour.Scores.Score;
import org.minecrafttest.main.Parkour.Scores.ScoreListener;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;
import org.minecrafttest.main.Watches.Chronometer;
import org.minecrafttest.main.Particles.ParticleAnimation;
import org.minecrafttest.main.Particles.ParticleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//ItemHandler
public class ItemHandler extends JavaPlugin {
    private static ItemHandler instance;
    private Config pluginConfig;
    private PlayerInteractionListener listener;
    private GameCommandExecutor executor;
    private ParticleAnimation particleAnimation;
    private Chronometer chronometer;
    private Parkour parkour;
    private Score score;
    private ScoreListener scoreListener;
    private Database database;
    private Hologram hologram;

    @Override
    public void onEnable() {
        instance = this;
        pluginConfig = new Config();
        pluginConfig.loadConfig();
        listener = new PlayerInteractionListener();
        executor = new GameCommandExecutor();
        particleAnimation = new ParticleAnimation();
        chronometer = new Chronometer(instance);
        parkour = new Parkour();
        score = new Score();
        scoreListener = new ScoreListener();
        database = new Database();
        hologram = new Hologram();

        hologram.init();

        Bukkit.getServer().getPluginManager().registerEvents(scoreListener, instance);
        Bukkit.getServer().getPluginManager().registerEvents(new ParkourListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new ParticleListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new ScoreListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(new HoloListener(), instance);
        Bukkit.getServer().getPluginManager().registerEvents(listener, instance);

        Objects.requireNonNull(getCommand("hologram")).setExecutor(new HologramCommand());

        List<String> aliases = new ArrayList<>();
        aliases.add("ih");
        PluginCommand command = getCommand("itemHandler");
        Objects.requireNonNull(command).setAliases(aliases);
        command.setExecutor(executor);

        listener.loadAllResources();
        listener.runThreads();

        MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();
        messageBuilder.append("[" + this.getName() + "] ", ColorText.GREEN)
                .append("Load: ")
                .append("Ok", ColorText.GREEN).build();

        messageBuilder.BukkitSender();

    }

    @Override
    public void onDisable() {
        listener.cancelAllMaterialChangeTasks();

        MessageBuilder messageBuilder = MessageBuilder.createMessageBuilder();
        messageBuilder.append("[" + this.getName() + "] ", ColorText.BLUE)
                .append("Close " + this.getName() , ColorText.RED).build();
        messageBuilder.BukkitSender();

    }
    public Config getCustomConfig(){
        return pluginConfig;
    }
    public PlayerInteractionListener getListener(){return listener;}
    public ParticleAnimation getParticleAnimation(){return particleAnimation;}
    public GameCommandExecutor getExecutor(){return executor;}
    public Chronometer getChronometer(){return chronometer;}
    public Parkour getParkour(){return parkour;}
    public Score getScore(){return score;}
    public ScoreListener getScoreListener(){return scoreListener;}
    public Database getDatabase(){return database;}
    public Hologram getHologram(){return hologram;}
    public static ItemHandler getPlugin() {
        return instance;
    }
}