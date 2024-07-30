package org.minecrafttest.main.Parkour.Scores;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Parkour.Checkpoint;
import org.minecrafttest.main.Parkour.Parkour;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreListener implements Listener {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final Parkour parkour = plugin.getParkour();
    private final Score score = plugin.getScore();
    private final Map<Player, PlayerInfo> playerInfo = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String parkourName = parkour.getPlayerParkour(player);

        if (parkourName != null && plugin.getChronometer().isRunChronometer(player)) {
            List<Checkpoint> checkpoints = parkour.getCheckpointsMap().get(parkourName);
            if (checkpoints != null) {
                for (int i = 0; i < checkpoints.size(); i++) {
                    Checkpoint checkpoint = checkpoints.get(i);
                    if (isLocationMatch(checkpoint.getLocation(), player.getLocation())) {
                        score.updateScore(player, i);
                        break;
                    }
                }

                double completionPercentage = score.getCompletionPercentage(player, checkpoints);
                double overallCompletionPercentage = score.getOverallCompletionPercentage(player, checkpoints);
                List<Player> rankings = score.getRankings(checkpoints, true);

                int position = rankings.indexOf(player) + 1; // Calculate the position of the player

                PlayerInfo info = new PlayerInfo(completionPercentage, overallCompletionPercentage, position);
                playerInfo.put(player, info);

                System.out.println("Player " + player.getName() + " has completed " + completionPercentage + "% towards the next checkpoint");
                System.out.println("Player " + player.getName() + " has completed " + overallCompletionPercentage + "% of the entire parkour");
                System.out.println("Player " + player.getName() + " is in position " + position);

                StringBuilder sb = new StringBuilder("Rankings:\n");
                for (int i = 0; i < rankings.size(); i++) {
                    Player rankedPlayer = rankings.get(i);
                    sb.append((i + 1)).append(". ").append(rankedPlayer.getName()).append("\n");
                }
                System.out.println(sb);
            }
        }
    }

    protected Map<Player, PlayerInfo> getPlayerInfo() {
        return playerInfo;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        score.removePlayerScore(player);
    }

    private boolean isLocationMatch(Location checkpointLocation, Location playerLocation) {
        return checkpointLocation.getWorld().equals(playerLocation.getWorld()) &&
                checkpointLocation.getBlockX() == playerLocation.getBlockX() &&
                checkpointLocation.getBlockY() == playerLocation.getBlockY() &&
                checkpointLocation.getBlockZ() == playerLocation.getBlockZ();
    }
}
