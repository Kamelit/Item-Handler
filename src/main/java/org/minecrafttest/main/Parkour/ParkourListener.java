package org.minecrafttest.main.Parkour;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.minecrafttest.main.ItemHandler;

import java.util.*;

public class ParkourListener implements Listener {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final Map<Player, Checkpoint> playerLastCheckpoint;

    public ParkourListener() {
        this.playerLastCheckpoint = plugin.getParkour().getPlayerLastCheckpoint();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        String parkourName = plugin.getParkour().getPlayerParkour(player);
        if (parkourName != null && plugin.getChronometer().isRunChronometer(player)) {
            List<Checkpoint> checkpoints = plugin.getParkour().getCheckpointsMap().get(parkourName);
            if (checkpoints != null) {
                for (Checkpoint checkpoint : checkpoints) {
                    if (isLocationMatch(checkpoint.getLocation(), location)) {
                        checkpoint.getPlayers().computeIfAbsent(parkourName, k -> new HashSet<>()).add(player);
                        playerLastCheckpoint.put(player, checkpoint);
                        return; // Exit after finding a matching checkpoint
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getChronometer().isRunChronometer(player)) {
            Location location = player.getLocation();
            if (hasPlayerPositionCorrect(location, player)) {
                Checkpoint lastCheckpoint = playerLastCheckpoint.get(player);
                if (lastCheckpoint != null) {
                    Location lastLocation = lastCheckpoint.getLocation();
                    lastLocation.setX(lastLocation.getBlockX() + 0.5);
                    lastLocation.setY(lastLocation.getBlockY() + 0.5);
                    lastLocation.setZ(lastLocation.getBlockZ() + 0.5);
                    float yaw = player.getLocation().getYaw();
                    float pitch = player.getLocation().getPitch();
                    lastLocation.setYaw(yaw);
                    lastLocation.setPitch(pitch);
                    plugin.getParkour().SetCheckpointInParkour(lastLocation);
                    player.teleportAsync(lastLocation);
                }
            }
        }
    }

    private boolean isLocationMatch(Location checkpointLocation, Location playerLocation) {
        return checkpointLocation.getWorld().equals(playerLocation.getWorld()) &&
                checkpointLocation.getBlockX() == playerLocation.getBlockX() &&
                checkpointLocation.getBlockY() == playerLocation.getBlockY() &&
                checkpointLocation.getBlockZ() == playerLocation.getBlockZ();
    }

    private boolean hasPlayerPositionCorrect(Location location, Player player) {
        Checkpoint lastCheckpoint = playerLastCheckpoint.get(player);
        if (lastCheckpoint != null) {
            return location.getY() <= lastCheckpoint.getMinY() || location.getY() >= lastCheckpoint.getMaxY();
        }
        return false;
    }


}
