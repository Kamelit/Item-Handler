package org.minecrafttest.main.Version;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.minecrafttest.main.Version.ItemsAsign.LegacyMetadataBuilder;
import org.minecrafttest.main.Version.ItemsAsign.ModernMetadataBuilder;

public interface MetadataBuilder {

    MetadataBuilder getPlugin(JavaPlugin plugin);
    <T> void setCustomData(ItemStack item, String key, @NotNull T value);
    <T> boolean hasCustomData(ItemStack item, String key, Class<T> clazz);
    <T> T getCustomData(ItemStack item, String key, Class<T> clazz);

    static MetadataBuilder createMetaBuilder(){
        if (APICompatibility.isModernAPIMeta()){
            return new ModernMetadataBuilder();
        }else {
            return new LegacyMetadataBuilder();
        }
    }
}