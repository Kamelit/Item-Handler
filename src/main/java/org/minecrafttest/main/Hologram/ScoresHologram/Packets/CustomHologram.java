package org.minecrafttest.main.Hologram.ScoresHologram.Packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Version.ArmorBuilder;

import java.util.*;

public final class CustomHologram {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final ArmorBuilder armorBuilder = ArmorBuilder.createArmorBuilder().SerializerCodesColor('&');
    public final Map<Player, Map<String, Integer>> customPacketHologram = new HashMap<>();
    private final Map<String, Location> locationMap = new HashMap<>();

    public void createCustomHologramUniquePlayer(Player player, String text) {
        locationMap.forEach((name, loc) -> {
                    if (loc != null) {
                        final UUID armorStandUUID = UUID.randomUUID(); // unique UUID
                        final int entityId = (int) (Math.random() * Integer.MAX_VALUE);
                        sendHologramPacket(name ,player, loc, text, armorStandUUID, entityId);
                    }
                }
        );
    }

    public void keyLocation(String key, Location location){locationMap.put(key, location);}

    private void sendHologramPacket(String key , Player player, Location location, String text, UUID uuid, int entityId) {
        PacketContainer spawnPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getModifier().writeDefaults();
        spawnPacket.getIntegers().write(0, entityId);// ID
        spawnPacket.getUUIDs().write(0, uuid);// UUID
        spawnPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);//Type
        spawnPacket.getDoubles().write(0, location.getX()) // X
                .write(1, location.getY()) // Y
                .write(2, location.getZ()); // Z

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, spawnPacket);
            customPacketHologram.computeIfAbsent(player, f -> new HashMap<>()).put(key, entityId);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }

        PacketContainer packetMetadata = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packetMetadata.getModifier().writeDefaults();
        packetMetadata.getIntegers().write(0, entityId);

        WrappedDataWatcher metadata = new WrappedDataWatcher();


        Optional<?> opt = Optional.of(WrappedChatComponent.fromChatMessage(armorBuilder.SerializeWitchPacketsArmor(text))[0].getHandle());


        WrappedDataWatcher.WrappedDataWatcherObject customName = new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true));
        WrappedDataWatcher.WrappedDataWatcherObject customNameVisible = new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class));
        WrappedDataWatcher.WrappedDataWatcherObject invisible = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));

        metadata.setObject(customName, opt);
        metadata.setObject(customNameVisible, true);
        metadata.setObject(invisible, (byte) 0x20);

        List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
        for (WrappedWatchableObject entry : metadata.getWatchableObjects()) {
            if (entry == null) continue;
            WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
            wrappedDataValueList.add(new WrappedDataValue(
                    watcherObject.getIndex(),
                    watcherObject.getSerializer(),
                    entry.getRawValue()
            ));
        }
        packetMetadata.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetMetadata);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public void UpdatePacketsLocation(String key , Location newLocation){
        locationMap.put(key, newLocation);
        customPacketHologram.forEach((player, hologramData)->{
            int entityId = hologramData.get(key);
            updatePacketLocation(player,entityId,newLocation);
        });
    }

    private void updatePacketLocation(Player player, int entityId, Location newLocation) {
        PacketContainer teleportPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        teleportPacket.getIntegers().write(0, entityId); // Entity ID
        teleportPacket.getDoubles().write(0, newLocation.getX()) // X
                .write(1, newLocation.getY()) // Y
                .write(2, newLocation.getZ()); // Z

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, teleportPacket);
        } catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }


}