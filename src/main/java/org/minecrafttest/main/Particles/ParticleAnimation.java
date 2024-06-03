package org.minecrafttest.main.Particles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.google.common.util.concurrent.AtomicDouble;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class ParticleAnimation {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public final Map<String, ScheduledTask> tasksParticles = new HashMap<>();
    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final PlayerInteractionListener listener = plugin.getListener();

    public void playAnimation(String key, Block block, TypesAnimation animation, int vision) {
        if (tasksParticles.get(key) == null) {
            switch (animation) {
                case BIG_BANG:
                case SQUARE:
                    break;
                case CIRCLE_POINT:
                    CirclePoint(block, key, vision);
                    break;
            }
        }
    }

    private void CirclePoint(Block block, String key, int vision) {
        AtomicDouble rotationAngle = new AtomicDouble(0.0);
        AtomicInteger r = new AtomicInteger(0);
        AtomicInteger g = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);
        String cacheBlock = listener.worldConfig.getString(key + ".blockType");
        Location location = block.getLocation();
        AtomicLong delay = new AtomicLong(400L);
        AtomicBoolean restartTask = new AtomicBoolean(false);
        AtomicReference<Consumer<ScheduledTask>> taskRef = new AtomicReference<>();
        Consumer<ScheduledTask> task = (ScheduledTask scheduledTask) -> {
            Bukkit.getRegionScheduler().execute(plugin, location, () -> {
                r.set(255);
                g.set(0);
                b.set(0);
                if (location.getBlock().getType().toString().equals(cacheBlock)) {
                    if (!listener.taskMap.get("parkour").isEmpty()) {
                        r.set(255);
                        g.set(255);
                        b.set(255);
                        if (delay.get() != 800L) {
                            delay.set(800L);
                            restartTask.set(true);
                        }
                    } else {
                        r.set(0);
                        g.set(255);
                        b.set(0);
                        if (delay.get() != 400L) {
                            delay.set(400L);
                            restartTask.set(true);
                        }
                    }
                } else {
                    if (delay.get() != 1200L) {
                        delay.set(1200L);
                        restartTask.set(true);
                    }
                }
            });
            double radius = 1.0;
            double angleIncrement = Math.PI / 20.0;
            double blockCenterX = block.getX() + 0.5;
            double blockCenterZ = block.getZ() + 0.5;
            for (int i = 0; i < 1; i++) {
                double angle = rotationAngle.get() + i * angleIncrement;
                double xOffset = blockCenterX + radius * Math.cos(angle);
                double zOffset = blockCenterZ + radius * Math.sin(angle);
                Location particleLocation = new Location(block.getWorld(), xOffset, block.getY(), zOffset);


                List<Player> nearbyPlayers = getNearbyPlayers(particleLocation, block.getChunk(), vision); // Adjust the radius of vision
                sendCustomParticles(nearbyPlayers, particleLocation, Color.fromRGB(r.get(), g.get(), b.get()));
            }
            double newAngle = rotationAngle.addAndGet(angleIncrement);
            if (newAngle >= Math.PI * 2) rotationAngle.set(0.0);
            if (restartTask.getAndSet(false)) {
                tasksParticles.get(key).cancel();
                tasksParticles.put(key, Bukkit.getAsyncScheduler().runAtFixedRate(plugin, taskRef.get(), 0, delay.get(), TimeUnit.MILLISECONDS));
            }
        };
        taskRef.set(task);
        tasksParticles.put(key, Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, 0, delay.get(), TimeUnit.MILLISECONDS));
    }

    private List<Player> getNearbyPlayers(Location location, Chunk chunk, int radiusChunks) {
        int chunkRadiusSquared = radiusChunks * radiusChunks;

        return location.getWorld().getPlayers().stream()
                .filter(player -> {
                    int playerChunkX = player.getLocation().getChunk().getX();
                    int playerChunkZ = player.getLocation().getChunk().getZ();
                    int dx = playerChunkX - chunk.getX();
                    int dz = playerChunkZ - chunk.getZ();
                    return dx * dx + dz * dz <= chunkRadiusSquared;
                })
                .collect(Collectors.toList());
    }

    private void sendCustomParticles(List<Player> players, Location location, Color color) {
        WrappedParticle<Particle.DustOptions> particle = WrappedParticle.create(Particle.REDSTONE, new Particle.DustOptions(color, (float) 1.0));

        PacketContainer particlePacket = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        particlePacket.getNewParticles().write(0, particle);
        particlePacket.getBooleans().write(0, false); // Long distance
        particlePacket.getDoubles().write(0, location.getX()); // X coordinate
        particlePacket.getDoubles().write(1, location.getY()); // Y coordinate
        particlePacket.getDoubles().write(2, location.getZ()); // Z coordinate
        particlePacket.getFloat().write(0, 0.0F); // Offset X
        particlePacket.getFloat().write(1, 0.0F); // Offset Y
        particlePacket.getFloat().write(2, 0.0F); // Offset Z
        particlePacket.getFloat().write(3, 1.0F); // Max Speed
        particlePacket.getIntegers().write(0, 1); // Particle Count


        try {
            for (Player player : players) {
                protocolManager.sendServerPacket(player, particlePacket);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error sending particle packet", e);
        }
    }

    public void RemoveTaskingParticles(String key) {
        if (tasksParticles.containsKey(key)) {
            if (tasksParticles.get(key) != null) {
                tasksParticles.get(key).cancel();
                tasksParticles.remove(key);
            }
        }
    }

    public void RemoveAllTaskingParticles() {
        for (ScheduledTask task : tasksParticles.values()) {
            task.cancel();
        }
        tasksParticles.clear();
    }
}
