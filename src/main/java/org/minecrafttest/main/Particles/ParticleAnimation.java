package org.minecrafttest.main.Particles;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedParticle;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Listener.PlayerInteractionListener;
import org.minecrafttest.main.Parkour.Checkpoint;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    public void playAnimation(String task_id, Block block, TypesAnimation animation, int vision) {
        if (tasksParticles.get(task_id) == null) {
            switch (animation) {
                case BIG_BANG:
                case BEAUTIFUL_CHECKPOINT:
                    break;
                case CIRCLE_POINT:
                    CirclePointTask(task_id, block, vision);
                    break;
            }
        }
    }

    public void playAnimation(String key, List<Checkpoint> checkpoints, TypesAnimation animation) {
        if (tasksParticles.get(key) == null) {
            if (Objects.requireNonNull(animation) == TypesAnimation.BEAUTIFUL_CHECKPOINT) {
                BeautifulCheckPoints(key, checkpoints);
            }
        }
    }


    private void CirclePointTask(String key, Block block, int vision) {
        AtomicReference<Double> rotationAngle = new AtomicReference<>(0.0);
        AtomicInteger r = new AtomicInteger(255);
        AtomicInteger g = new AtomicInteger();
        AtomicInteger b = new AtomicInteger();
        String cacheBlock = listener.worldConfig.getString(key + ".blockType");
        Location location = block.getLocation();
        AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
        AtomicReference<Double> speed = new AtomicReference<>(1.0); // Initial speed of rotation

        Consumer<ScheduledTask> task = scheduledTask -> Bukkit.getRegionScheduler().execute(plugin, location, () -> {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUpdateTime.get();
            lastUpdateTime.set(currentTime);

            // Determine block type and adjust colors and speed
            if (location.getBlock().getType().toString().equals(cacheBlock)) {
                if (!listener.taskMap.get("parkour").isEmpty()) {
                    r.set(255);
                    g.set(255);
                    b.set(255);
                    speed.set(0.5); // Speed of rotation when the block matches and there's parkour
                } else {
                    r.set(0);
                    g.set(255);
                    b.set(0);
                    speed.set(1.0); // Speed of rotation when the block matches and there's no parkour
                }
            } else {
                r.set(255);
                g.set(0);
                b.set(0);
                speed.set(0.25); // Speed of rotation when the block doesn't match
            }

            double radius = 1.0;
            double angleIncrement = Math.PI / 40.0 * speed.get(); // Adjust rotation speed, more points on the circle
            double blockCenterX = block.getX() + 0.5;
            double blockCenterZ = block.getZ() + 0.5;

            for (int part = 0; part < 4; part++) { // Four parts of the circle
                double partOffset = (Math.PI / 2) * part; // Offset each part by 90 degrees
                for (int i = 0; i < 2; i++) { // Generate i particles for each part
                    double angle = rotationAngle.get() + partOffset + i * angleIncrement + elapsedTime * angleIncrement / 150; // Adjust based on time and position
                    double xOffset = blockCenterX + radius * Math.cos(angle);
                    double zOffset = blockCenterZ + radius * Math.sin(angle);
                    Location particleLocation = new Location(block.getWorld(), xOffset, block.getY(), zOffset);

                    List<Player> nearbyPlayers = getNearbyPlayers(particleLocation, block.getChunk(), vision); // Adjust vision radius
                    sendCustomParticles(nearbyPlayers, particleLocation, Color.fromRGB(r.get(), g.get(), b.get()));
                }
            }

            rotationAngle.updateAndGet(v -> v + angleIncrement);
            if (rotationAngle.get() >= Math.PI * 2) {
                rotationAngle.updateAndGet(v -> v - Math.PI * 2);
            }
        });

        tasksParticles.put(key, Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, 0, 150, TimeUnit.MILLISECONDS));
    }


    public void BeautifulCheckPoints(String key, List<Checkpoint> checkpoints) {
        Consumer<ScheduledTask> task = scheduledTask -> {
            Set<Player> playerInChronometer = plugin.getChronometer().getPlayersInChronometer();
            if (playerInChronometer.isEmpty()) {
                tasksParticles.remove(key);
                scheduledTask.cancel();
                return;
            }

            for (Checkpoint checkpoint : checkpoints) {

                double radius = 1.5; // Radio de la espiral
                double height = 3.0; // Altura total de la espiral
                int turns = 3; // Número de vueltas de la espiral
                int numParticles = 20; // Particles Number
                double delta = 2 * Math.PI * turns / numParticles; // Diferencia angular entre partículas
                double rotationSpeed = Math.PI / 4; // Speed Rotations Radians
                double currentTime = System.currentTimeMillis() / 1000.0; // Tiempo actual en segundos

                // Interpolar color
                float hue = ((System.currentTimeMillis() ) % 10000) / 10000.0f;
                java.awt.Color awtColor = java.awt.Color.getHSBColor(hue, 1.0f, 1.0f);
                int rgb = awtColor.getRGB() & 0xFFFFFF; // Rango
                Color color = Color.fromRGB(rgb);

                Location checkpointLocation = checkpoint.getLocation().clone().add(0.5, 0.5, 0.5); // Center

                if (checkpoint.getPlayers().containsKey(key)) {
                    Set<Player> playersInCheckpoint = checkpoint.getPlayers().get(key);

                    if (playersInCheckpoint != null) {
                        List<Player> playersNotInCheckpoint = new ArrayList<>(playerInChronometer);
                        playersNotInCheckpoint.removeAll(playersInCheckpoint);

                        for (int i = 0; i < numParticles; i++) { // Number
                            double angle = i * delta;
                            double x = radius * Math.cos(angle);
                            double y = (height / numParticles) * i - (height / 2); // Alto
                            double z = radius * Math.sin(angle);

                            // Rotate
                            double rotationAngle = currentTime * rotationSpeed;
                            double rotatedX = x * Math.cos(rotationAngle) - z * Math.sin(rotationAngle);
                            double rotatedZ = x * Math.sin(rotationAngle) + z * Math.cos(rotationAngle);

                            Location particleLocation = checkpointLocation.clone().add(rotatedX, y, rotatedZ);
                            sendCustomParticles(playersNotInCheckpoint, particleLocation, color);
                        }
                    }
                } else {
                    for (int i = 0; i < numParticles; i++) { // Number
                        double angle = i * delta;
                        double x = radius * Math.cos(angle);
                        double y = (height / numParticles) * i - (height / 2); // Alto
                        double z = radius * Math.sin(angle);

                        // Rotate
                        double rotationAngle = currentTime * rotationSpeed;
                        double rotatedX = x * Math.cos(rotationAngle) - z * Math.sin(rotationAngle);
                        double rotatedZ = x * Math.sin(rotationAngle) + z * Math.cos(rotationAngle);

                        Location particleLocation = checkpointLocation.clone().add(rotatedX, y, rotatedZ);
                        sendCustomParticles(new ArrayList<>(playerInChronometer), particleLocation, color);
                    }
                }
            }
        };
        tasksParticles.put(key, Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task, 0, 150L, TimeUnit.MILLISECONDS));
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
