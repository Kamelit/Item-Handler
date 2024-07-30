package org.minecrafttest.main.Parkour.Scores;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Parkour.Checkpoint;

import java.util.*;

public class Score {

    private final Map<Player, Integer> playerScores = new HashMap<>();
    private final ScoreListener scoreListener = ItemHandler.getPlugin().getScoreListener();

    protected void updateScore(Player player, int checkpointIndex) {
        playerScores.put(player, checkpointIndex);
    }

    public void removePlayerScore(Player player){
        playerScores.remove(player);
    }

    protected List<Player> getRankings(List<Checkpoint> checkpoints, boolean precision) {
        List<Map.Entry<Player, Integer>> sortedScores = new ArrayList<>(playerScores.entrySet());

        sortedScores.sort((e1, e2) -> {
            int comparison = e2.getValue().compareTo(e1.getValue());
            if (precision && comparison == 0) {
                Player player1 = e1.getKey();
                Player player2 = e2.getKey();
                int nextCheckpointIndex1 = e1.getValue() + 1;
                int nextCheckpointIndex2 = e2.getValue() + 1;

                if (nextCheckpointIndex1 < checkpoints.size() && nextCheckpointIndex2 < checkpoints.size()) {
                    Location nextCheckpointLoc1 = checkpoints.get(nextCheckpointIndex1).getLocation();
                    Location nextCheckpointLoc2 = checkpoints.get(nextCheckpointIndex2).getLocation();
                    double distance1 = calculateDistance(player1.getLocation(), nextCheckpointLoc1);
                    double distance2 = calculateDistance(player2.getLocation(), nextCheckpointLoc2);
                    return Double.compare(distance1, distance2);
                }
            }
            return comparison;
        });

        List<Player> rankings = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : sortedScores) {
            rankings.add(entry.getKey());
        }
        return rankings;
    }

    protected double getCompletionPercentage(Player player, List<Checkpoint> checkpoints) {
        if (!playerScores.containsKey(player)) {
            return 0.0;
        }

        int currentCheckpointIndex = playerScores.get(player);
        if (currentCheckpointIndex < 0 || currentCheckpointIndex >= checkpoints.size() - 1) {
            return 0.0;
        }

        Checkpoint currentCheckpoint = checkpoints.get(currentCheckpointIndex);
        Checkpoint nextCheckpoint = checkpoints.get(currentCheckpointIndex + 1);
        double distanceToNext = calculateDistance(player.getLocation(), nextCheckpoint.getLocation());
        double totalDistance = calculateDistance(currentCheckpoint.getLocation(), nextCheckpoint.getLocation());
        double percentage = (1.0 - (distanceToNext / totalDistance)) * 100.0;

        return Math.max(0.0, Math.min(100.0, percentage));
    }

    protected double getOverallCompletionPercentage(Player player, List<Checkpoint> checkpoints) {
        if (!playerScores.containsKey(player)) {
            return 0.0;
        }

        int currentCheckpointIndex = playerScores.get(player);
        int totalCheckpoints = checkpoints.size();

        if (totalCheckpoints == 0) {
            return 0.0;
        }

        if (currentCheckpointIndex == totalCheckpoints - 1) {
            return 100.0;
        }

        Checkpoint currentCheckpoint = checkpoints.get(currentCheckpointIndex);
        Checkpoint nextCheckpoint = checkpoints.get(currentCheckpointIndex + 1);
        double distanceToNext = calculateDistance(player.getLocation(), nextCheckpoint.getLocation());
        double totalDistance = calculateDistance(currentCheckpoint.getLocation(), nextCheckpoint.getLocation());
        double segmentPercentage = (1.0 - (distanceToNext / totalDistance)) * (100.0 / (totalCheckpoints - 1));
        double overallPercentage = (currentCheckpointIndex * (100.0 / (totalCheckpoints - 1))) + segmentPercentage;

        return Math.max(0.0, Math.min(100.0, overallPercentage));
    }

    private double calculateDistance(Location loc1, Location loc2) {
        double x1 = loc1.getX();
        double y1 = loc1.getY();
        double z1 = loc1.getZ();
        double x2 = loc2.getX();
        double y2 = loc2.getY();
        double z2 = loc2.getZ();
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public Map<Player, PlayerInfo> getPlayerInfo() {
        if (scoreListener == null) {
            return Collections.emptyMap();
        }
        return scoreListener.getPlayerInfo();
    }

}
