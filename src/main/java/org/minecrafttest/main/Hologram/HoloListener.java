package org.minecrafttest.main.Hologram;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.minecrafttest.main.Hologram.ScoresHologram.Packets.CustomHologram;

public final class HoloListener implements Listener {

    private final CustomHologram customHologram = new CustomHologram();

    @EventHandler
    public void onPlayerJoinScores(PlayerJoinEvent event){
        Player player = event.getPlayer();
        customHologram.createCustomHologramUniquePlayer(player, "Player yes: " + player.getName());
    }


}
