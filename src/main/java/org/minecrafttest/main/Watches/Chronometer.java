package org.minecrafttest.main.Watches;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Chronometer implements Runnable {
    private final JavaPlugin plugin;
    private final ConcurrentHashMap<UUID, Integer> playerStartTime;
    private final ConcurrentHashMap<UUID, Integer> playerElapsedTime;
    private final ConcurrentHashMap<UUID, Boolean> playerRunning;
    private final ConcurrentHashMap<UUID, Boolean> playerCountdownMode;
    private final ConcurrentHashMap<UUID, Integer> playerMaxTimeMillis;
    private final Set<Player> playersInChronometer;
    private static final int RESET_INTERVAL = 24 * 60 * 60 * 1000;
    private static final int BLINK_THRESHOLD = 7500;
    private static final int TASK_INTERVAL = 123;
    private int blinkTick;
    private ScheduledTask task;

    public Chronometer(JavaPlugin plugin) {
        this.plugin = plugin;
        playerStartTime = new ConcurrentHashMap<>();
        playerElapsedTime = new ConcurrentHashMap<>();
        playerRunning = new ConcurrentHashMap<>();
        playerCountdownMode = new ConcurrentHashMap<>();
        playerMaxTimeMillis = new ConcurrentHashMap<>();
        playersInChronometer = new HashSet<>();
        blinkTick = 0;
    }

    //public void startChronometer(Player player) {startChronometer(player, 0, 30, false);}

    //public void startChronometer(Player player, boolean countdownMode) {startChronometer(player, 0, 30, countdownMode);}

    public void startChronometer(Player player, int minutes, int seconds) {startChronometer(player, minutes, seconds, false);}

    public void startChronometer(Player player, int minutes, int seconds, boolean countdownMode) {
        UUID playerUUID = player.getUniqueId();

        if (playerRunning.getOrDefault(playerUUID, false)) {
            //player.sendMessage("El chronometer ya está en start.");
            return;
        }

        minutes = Math.abs(minutes);
        seconds = Math.abs(seconds);
        int maxTimeMillis = (minutes * 60 + seconds) * 1000;

        playerStartTime.put(playerUUID, (int) System.currentTimeMillis());
        playerElapsedTime.put(playerUUID, 0);
        playerRunning.put(playerUUID, true);
        playerCountdownMode.put(playerUUID, countdownMode);
        playerMaxTimeMillis.put(playerUUID, maxTimeMillis);
        playersInChronometer.add(player);

        //player.sendMessage("El cronómetro ha comenzado en " + (countdownMode ? "modo cuenta regresiva." : "modo cuenta progresiva.") + " con un tiempo límite de " + minutes + " minutos y " + seconds + " segundos.");

        if (task == null || task.isCancelled()) {
            task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task1 -> run(), 0, TASK_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    public void stopChronometer(Player player) {
        UUID playerUUID = player.getUniqueId();

        if (!playerRunning.getOrDefault(playerUUID, false)) {
            //player.sendMessage("El cronómetro no está en marcha.");
            return;
        }

        // Limpieza de datos del jugador
        playerRunning.remove(playerUUID);
        playerStartTime.remove(playerUUID);
        playerElapsedTime.remove(playerUUID);
        playerCountdownMode.remove(playerUUID);
        playerMaxTimeMillis.remove(playerUUID);
        playersInChronometer.remove(player);

        //player.sendMessage("El cronómetro se ha detenido y los datos han sido limpiados.");

        if (playerRunning.values().stream().noneMatch(Boolean::booleanValue)) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }
    }

    public boolean isRunChronometer(Player player) {
        UUID playerUUID = player.getUniqueId();
        return playerRunning.getOrDefault(playerUUID, false);
    }

    @Override
    public void run() {
        int currentTime = (int) System.currentTimeMillis();

        for (UUID playerUUID : playerRunning.keySet()) {
            if (Objects.requireNonNull(Bukkit.getPlayer(playerUUID)).isConnected()){
                if (playerRunning.get(playerUUID)) {
                    boolean isCountdown = playerCountdownMode.getOrDefault(playerUUID, false);
                    int startTime = playerStartTime.get(playerUUID);
                    int previousElapsed = playerElapsedTime.get(playerUUID);
                    int elapsed = currentTime - startTime + previousElapsed;

                    int maxTimeMillis = playerMaxTimeMillis.get(playerUUID);
                    int displayElapsed = elapsed;

                    if (isCountdown) {
                        displayElapsed = maxTimeMillis - elapsed;
                    }

                    if (elapsed >= RESET_INTERVAL) {
                        startTime = currentTime;
                        elapsed = previousElapsed = 0;
                        playerStartTime.put(playerUUID, startTime);
                        playerElapsedTime.put(playerUUID, previousElapsed);
                    }

                    int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(displayElapsed);
                    int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(displayElapsed) % 60;
                    int milliseconds = displayElapsed % 1000;

                    if (isCountdown && displayElapsed <= 0) {
                        playerRunning.put(playerUUID, false);
                        minutes = 0;
                        seconds = 0;
                        milliseconds = 0;
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.sendActionBar(Component.text(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds)).color(TextColor.color(255, 0, 0)));
                            player.sendMessage("El cronómetro se ha detenido automáticamente.");
                        }

                        playerRunning.remove(playerUUID);
                        playerStartTime.remove(playerUUID);
                        playerElapsedTime.remove(playerUUID);
                        playerCountdownMode.remove(playerUUID);
                        playerMaxTimeMillis.remove(playerUUID);
                        playersInChronometer.remove(player);

                        continue;
                    } else if (!isCountdown && elapsed >= maxTimeMillis) {
                        playerRunning.put(playerUUID, false);
                        minutes = maxTimeMillis / 60000;
                        seconds = (maxTimeMillis / 1000) % 60;
                        milliseconds = 0;
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null) {
                            player.sendActionBar(Component.text(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds)).color(TextColor.color(255, 0, 0)));
                            player.sendMessage("El cronómetro se ha detenido automáticamente.");
                        }

                        playerRunning.remove(playerUUID);
                        playerStartTime.remove(playerUUID);
                        playerElapsedTime.remove(playerUUID);
                        playerCountdownMode.remove(playerUUID);
                        playerMaxTimeMillis.remove(playerUUID);
                        playersInChronometer.remove(player);
                        continue;
                    }

                    TextColor color = interpolateColor(isCountdown ? maxTimeMillis - displayElapsed : displayElapsed, maxTimeMillis);

                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        if ((isCountdown && displayElapsed <= BLINK_THRESHOLD) || (!isCountdown && displayElapsed >= maxTimeMillis - BLINK_THRESHOLD)) {
                            double opacityFactor = 0.5 + 0.5 * Math.sin(Math.PI * blinkTick / 2); // Limit Time blink
                            blinkTick++;
                            int red = (int) (color.red() * opacityFactor);
                            int green = (int) (color.green() * opacityFactor);
                            int blue = (int) (color.blue() * opacityFactor);
                            TextColor blinkColor = TextColor.color(red, green, blue);
                            player.sendActionBar(Component.text(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds))
                                    .color(blinkColor));
                        } else {
                            player.sendActionBar(Component.text(String.format("%02d:%02d:%03d", minutes, seconds, milliseconds)).color(color));
                        }
                    }
                }
            }else {
                playerRunning.remove(playerUUID);
                playerStartTime.remove(playerUUID);
                playerElapsedTime.remove(playerUUID);
                playerCountdownMode.remove(playerUUID);
                playerMaxTimeMillis.remove(playerUUID);
                playersInChronometer.remove(Bukkit.getPlayer(playerUUID));
            }
        }
        if (playerRunning.values().stream().noneMatch(Boolean::booleanValue)) {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

    }

    private TextColor interpolateColor(int elapsed, int maxTime) {
        float fraction = (float) elapsed / maxTime;
        int red, green;

        if (fraction < 0.5) {
            red = (int) (fraction * 2 * 255);
            green = 255;
        } else {
            red = 255;
            green = (int) ((1 - fraction) * 2 * 255);
        }

        return TextColor.color(red, green, 0);
    }

    public Set<Player> getPlayersInChronometer(){
        return playersInChronometer;
    }

    public int getTimeByPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!playerRunning.containsKey(playerUUID)) {
            return 0;
        }

        int currentTime = (int) System.currentTimeMillis();
        boolean isCountdown = playerCountdownMode.getOrDefault(playerUUID, false);
        int startTime = playerStartTime.get(playerUUID);
        int previousElapsed = playerElapsedTime.get(playerUUID);
        int elapsed = currentTime - startTime + previousElapsed;

        int displayElapsed = elapsed;
        if (isCountdown) {
            int maxTimeMillis = playerMaxTimeMillis.get(playerUUID);
            displayElapsed = maxTimeMillis - elapsed;
        }

        return displayElapsed;
    }
}
