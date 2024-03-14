package org.minecrafttest.main.Listener;

import java.util.List;
import java.util.Map;

public class MaterialMetadata {
    private final String material;
    private final String quantity;
    private final String name;
    private final String subcommand;
    private final List<String> subLore;
    private final boolean addItemOnClick;
    private final boolean setDrop;
    private final boolean changeSlot;
    private final String itemName;
    private final String subcommandRight;
    private final String subcommandLeft;
    private final Map<String, String> enchantments;
    private final boolean shotBow;
    private final boolean sub_wearArmor;
    private final boolean sub_deleteItemOnDeath;

    public MaterialMetadata(String material, String quantity, String name, String subcommand, String subcommandRight, String subcommandLeft, List<String> subLore, boolean addItemOnClick, boolean setDrop, boolean changeSlot, String itemName, Map<String, String>  enchantments, boolean shotBow, boolean sub_wearArmor, boolean sub_deleteItemOnDeath) {
        this.material = material;
        this.quantity = quantity;
        this.name = name;
        this.subcommand = subcommand;
        this.subcommandRight = subcommandRight;
        this.subcommandLeft = subcommandLeft;
        this.subLore = subLore;
        this.addItemOnClick = addItemOnClick;
        this.setDrop = setDrop;
        this.changeSlot = changeSlot;
        this.itemName = itemName;
        this.enchantments = enchantments;
        this.shotBow = shotBow;
        this.sub_wearArmor = sub_wearArmor;
        this.sub_deleteItemOnDeath = sub_deleteItemOnDeath;
    }

    public String getMaterial() {
        return material;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public String getSubcommand() {
        return subcommand;
    }

    public String getSubcommandRight() {
        return subcommandRight;
    }

    public String getSubcommandLeft() {
        return subcommandLeft;
    }

    public List<String> getSubLore() {
        return subLore;
    }

    public boolean isAddItemOnClick() {
        return addItemOnClick;
    }

    public boolean isSetDrop() {
        return setDrop;
    }

    public boolean isChangeSlot() {
        return changeSlot;
    }

    public String getItemName() {
        return itemName;
    }

    public Map<String, String> getEnchantments() {
        return enchantments;
    }

    public boolean shotBow() {
        return shotBow;
    }

    public boolean wearArmor() {
        return sub_wearArmor;
    }

    public boolean deleteItemOnDeath(){
        return sub_deleteItemOnDeath;
    }

}