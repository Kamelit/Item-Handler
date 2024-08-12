package org.minecrafttest.main.Version.Armors;

import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.minecrafttest.main.Version.ArmorBuilder;

@SuppressWarnings("deprecation")
public class LegacyArmorsStandsBuilder implements ArmorBuilder {

    private char colorChar;

    @Override
    public ArmorBuilder SerializerCodesColor(char character) {
        this.colorChar = character;
        return this;
    }

    @Override
    public void CustomNameArmor(ArmorStand armorStand, String text) {
        String coloredText = ChatColor.translateAlternateColorCodes(colorChar, text);
        armorStand.setCustomName(coloredText);
        armorStand.setCustomNameVisible(true);
    }

    @Override
    public String SerializeWitchPacketsArmor(String text) {
        return ChatColor.translateAlternateColorCodes(colorChar, text);
    }


}
