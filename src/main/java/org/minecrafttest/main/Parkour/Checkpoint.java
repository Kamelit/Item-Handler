package org.minecrafttest.main.Parkour;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Set;

public class Checkpoint {
    private final String map;
    private final String id;
    private final Location location;
    private final int minY;
    private final HashMap<String, Set<Player>> players;

    public Checkpoint(String map, String id, Location location, int minY) {
        this.map = map;this.id = id;this.location = location;this.minY = minY;this.players = new HashMap<>();
    }

    public String getMap (){ return map; }

    public String getId (){ return id; }

    public Location getLocation() {
        return location;
    }

    public int getMinY() {
        return minY;
    }

    public HashMap<String, Set<Player>> getPlayers() {
        return players;
    }
}
