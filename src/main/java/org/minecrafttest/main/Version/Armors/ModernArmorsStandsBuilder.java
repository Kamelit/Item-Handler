package org.minecrafttest.main.Version.Armors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.ArmorStand;
import org.minecrafttest.main.Version.ArmorBuilder;

public class ModernArmorsStandsBuilder implements ArmorBuilder {

    private LegacyComponentSerializer serializer;

    @Override
    public ArmorBuilder SerializerCodesColor(char character) {
        serializer = LegacyComponentSerializer.builder()
                .character(character)
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat().build();
        return this;
    }

    @Override
    public void CustomNameArmor(ArmorStand armorStand, String text) {
        Component component = serializer.deserialize(text);
        armorStand.customName(component);
        armorStand.setCustomNameVisible(true);
    }

    @Override
    public String SerializeWitchPacketsArmor(String text) {
        Component component = serializer.deserialize(text);
        return serializer.serialize(component);
    }
}
