package org.minecrafttest.main.Listener;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomMaterial {
    private final Material material;
    private final int quantity;
    private final String name;
    private final String subcommand;
    private final List<String> sub_lore;

    public CustomMaterial(Material material, int quantity, String name, String subcommand, List<String> sub_lore) {
        this.material = material;
        this.quantity = quantity;
        this.name = name;
        this.subcommand = subcommand;
        this.sub_lore = sub_lore;
    }

    public Material getMaterial() {
        return material;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public String getSubcommand() {
        return subcommand;
    }

    public List<String> getSub_lore() {
        return sub_lore;
    }

    public static CustomMaterial fromConfiguration(ConfigurationSection configSection) {
        Material material = Material.valueOf(configSection.getString("material", "DIRT"));
        int quantity = configSection.getInt("quantity", 1);
        String name = configSection.getString("name", "Item_Name");
        String subcommand = configSection.getString("subcommand", "default_subcommand");
        List<String> sub_lore = configSection.getStringList("sub_lore");
        return new CustomMaterial(material, quantity, name, subcommand, sub_lore);
    }

    public ItemStack toItemStack() {
        // Set custom metadata if needed
        return new ItemStack(material, quantity);
    }
}