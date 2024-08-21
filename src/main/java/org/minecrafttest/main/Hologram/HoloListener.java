package org.minecrafttest.main.Hologram;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.minecrafttest.main.Hologram.ScoresHologram.Packets.CustomHologram;
import org.minecrafttest.main.ItemHandler;

public final class HoloListener implements Listener {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final CustomHologram customHologram = plugin.getHologram().customHologram;

    @EventHandler
    public void onPlayerJoinScores(PlayerJoinEvent event){
        Player player = event.getPlayer();
        customHologram.createCustomHologramUniquePlayer(player, "Player yes: " + player.getName());
    }

    @EventHandler
    public void onPlayerDisconnected(PlayerQuitEvent event){
        Player player = event.getPlayer();
        customHologram.customPacketHologram.remove(player);
    }

}
