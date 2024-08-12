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
import org.minecrafttest.main.Hologram.Hologram;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Version.ArmorBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CustomHologram {

    private final ItemHandler plugin = ItemHandler.getPlugin();
    private final Hologram hologram = plugin.getHologram();
    private final ArmorBuilder armorBuilder = ArmorBuilder.createArmorBuilder().SerializerCodesColor('&');


    public void createCustomHologramUniquePlayer(Player player, String text) {
        hologram.getLocations().forEach(
                loc -> {
                    if (loc != null) {
                        final UUID armorStandUUID = UUID.randomUUID(); // unique UUID
                        final int entityId = (int) (Math.random() * Integer.MAX_VALUE);
                        sendHologramPacket(player, loc, text, armorStandUUID, entityId);
                    }
                }
        );
    }

    private void sendHologramPacket(Player player, Location location, String text, UUID uuid, int entityId) {
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
}