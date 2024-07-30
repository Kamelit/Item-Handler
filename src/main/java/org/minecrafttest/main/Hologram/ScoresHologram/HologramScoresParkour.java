package org.minecrafttest.main.Hologram.ScoresHologram;


import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minecrafttest.main.Database.Database;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Parkour.Scores.Score;
import org.minecrafttest.main.Watches.Chronometer;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HologramScoresParkour {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final Score score = plugin.getScore();
    //private Hologram hologram;
    private final Database database = plugin.getDatabase();
    private String[] letters;
    private final Chronometer chronometer = plugin.getChronometer();


    public void ShowScores(Location location){

        letters = new String[]{
                "Scores",
                "base 1",
                "base 2",
                "base 2",
                "..."
        };
        //hologram = new Hologram(letters);
        //hologram.spawn(location);
        Updates(location);
    }

    private void Updates(Location location){


        Consumer<ScheduledTask> task = Updates -> {
            for (int i = 1; i < letters.length; i++){
                //hologram.UpdateInfoHologram(i,database.getParkourDataLobby().getDataScoreByIndex(i - 1));
            }

        };
        Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task, 30, 20*3);
    }

    public String formatHologramStatus(Player player) {
        String name = player.getName();
        String position = "--";
        String time = "--:--:---";
        String percentageCheckpoint = "-%";
        String totalPercentage = "-%";

        if (score.getPlayerInfo().containsKey(player)) {
            position = String.valueOf(score.getPlayerInfo().get(player).getPosition());
            percentageCheckpoint = String.valueOf(score.getPlayerInfo().get(player).getCompletionPercentage());
            totalPercentage = String.valueOf(score.getPlayerInfo().get(player).getOverallCompletionPercentage());
            int displayElapsed = chronometer.getTimeByPlayer(player);

            int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(displayElapsed);
            int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(displayElapsed) % 60;
            int milliseconds = displayElapsed % 1000;

            time = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
        }

        return String.format("Name: %s, Position: %s, Time: %s, Checkpoint Completion: %s%%, Total Completion: %s%%",
                name, position, time, percentageCheckpoint, totalPercentage);
    }



}
