package org.minecrafttest.main.Version;

import org.bukkit.entity.ArmorStand;
import org.minecrafttest.main.Version.Armors.LegacyArmorsStandsBuilder;
import org.minecrafttest.main.Version.Armors.ModernArmorsStandsBuilder;

public interface ArmorBuilder {
    ArmorBuilder SerializerCodesColor(char character);
    void CustomNameArmor(ArmorStand armorStand, String text);
    String SerializeWitchPacketsArmor(String text);

    static ArmorBuilder createArmorBuilder(){
        if (APICompatibility.isModernAPIComponent()){
            return new ModernArmorsStandsBuilder();
        }else {
            return new LegacyArmorsStandsBuilder();
        }
    }
}