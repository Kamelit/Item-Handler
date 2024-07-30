package org.minecrafttest.main.Cache.types.ArmorStand;

import org.bukkit.Location;

import java.util.UUID;

public class ArmorStandData {
    private final UUID uuid;
    private final int id;
    private final Location location;

    public ArmorStandData(UUID uuid, int id, Location location) {
        this.uuid = uuid;
        this.id = id;
        this.location = location;
    }


    public UUID getUuid() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }
}