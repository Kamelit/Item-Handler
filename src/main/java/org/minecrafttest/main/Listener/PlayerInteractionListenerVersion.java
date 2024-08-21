package org.minecrafttest.main.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.minecrafttest.main.ItemHandler;

public class PlayerInteractionListenerVersion implements Listener {

    private final ItemHandler plugin = ItemHandler.getPlugin();

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!plugin.getCustomConfig().getChangeHand()) {
            event.setCancelled(true);
        }
    }
}
