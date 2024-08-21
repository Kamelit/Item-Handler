package org.minecrafttest.main.Cache.types.ArmorStand;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public final class ArmorStandDataTypeAdapter extends TypeAdapter<ArmorStandData> {

    @Override
    public void write(JsonWriter out, ArmorStandData data) throws IOException {
        out.beginObject();
        out.name("UUID").value(data.getUuid().toString());
        out.name("ID").value(data.getId());
        out.name("World").value(data.getLocation().getWorld().getName());
        out.name("X").value(data.getLocation().getX());
        out.name("Y").value(data.getLocation().getY());
        out.name("Z").value(data.getLocation().getZ());
        out.endObject();
    }

    @Override
    public ArmorStandData read(JsonReader in) throws IOException {
        UUID uuid = null;
        int id = -1;
        String world = null;
        double x = 0, y = 0, z = 0;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "UUID":
                    uuid = UUID.fromString(in.nextString());
                    break;
                case "ID":
                    id = in.nextInt();
                    break;
                case "World":
                    world = in.nextString();
                    break;
                case "X":
                    x = in.nextDouble();
                    break;
                case "Y":
                    y = in.nextDouble();
                    break;
                case "Z":
                    z = in.nextDouble();
                    break;
            }
        }
        in.endObject();

        Location location = new Location(Bukkit.getWorld(Objects.requireNonNull(world)), x, y, z);
        return new ArmorStandData(uuid, id, location);
    }
}