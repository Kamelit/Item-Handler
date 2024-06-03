package org.minecrafttest.main.Particles;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;

import java.util.*;

public class ParticleListener implements Listener {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final PlayerInteractionListener listener = plugin.getListener();

    private final Map<String, Set<Player>> playersInArea = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location locationPlayer = player.getLocation();

        for (Map.Entry<String, List<Object>> entry : listener.worldConfigInMemory.entrySet()) {
            String key = entry.getKey();
            List<Object> values = entry.getValue();
            int CCX = (int) values.get(2);
            int CCZ = (int) values.get(3);
            Block blockDestination = (Block) values.get(0);
            List<?> animation_checks = (List<?>) values.get(5);
            TypesAnimation animation = TypesAnimation.valueOf(animation_checks.get(0).toString());
            int vision = Integer.parseInt((animation_checks.get(1)).toString());

            if (isWithinChunkRadius(locationPlayer, CCX, CCZ, vision)) {
                plugin.getParticleAnimation().playAnimation(key, blockDestination, animation, vision);
                playersInArea.computeIfAbsent(key, k -> new HashSet<>()).add(player);
            } else {
                Set<Player> players = playersInArea.get(key);
                if (players != null) {
                    players.remove(player);
                    if (players.isEmpty()) {
                        plugin.getParticleAnimation().RemoveTaskingParticles(key);
                    }
                }
            }
        }
    }

    private boolean isWithinChunkRadius(Location locationPlayer, int CCX, int CCZ, int radius) {
        int chunkRadiusSquared = radius * radius;
        Chunk chunk = locationPlayer.getChunk();
        int dx = chunk.getX() - CCX;
        int dz = chunk.getZ() - CCZ;
        return dx * dx + dz * dz <= chunkRadiusSquared;
    }
}