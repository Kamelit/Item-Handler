package org.minecrafttest.main.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.minecrafttest.main.Main;

import java.util.ArrayList;
import java.util.List;




public class Config implements Listener {

    private final boolean clearInventory;
    private final boolean changeHand;
    private final FileConfiguration config;
    private final Server server;

    public Config(Server server, boolean clearInventory, boolean changeHand, FileConfiguration config){
        this.clearInventory = clearInventory;
        this.changeHand = changeHand;
        this.config = config;
        this.server = server;
    }

    NamespacedKey key1 = new NamespacedKey(Main.getInstance(), "set_drop");
    NamespacedKey key2 = new NamespacedKey(Main.getInstance(), "command_onclick");
    NamespacedKey key3 = new NamespacedKey(Main.getInstance(), "change_slot");
    NamespacedKey key4 = new NamespacedKey(Main.getInstance(), "add_item_on_click");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (clearInventory) {
            player.getInventory().clear();
        }
        if (config.contains("items")) {
            ConfigurationSection itemsConfig = config.getConfigurationSection("items");
            assert itemsConfig != null;
            for (String itemName : itemsConfig.getKeys(false)) {
                ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                assert itemConfig != null;
                ItemStack itemStack = createItemStackFromConfig(itemConfig);
                int slot = itemConfig.getInt("slot");
                player.getInventory().setItem(slot, itemStack);
            }
        }
    }

    private boolean allowItemPlacement(Material material) {
        return material == Material.CHEST || material.isInteractable() || material == Material.REDSTONE_TORCH || material == Material.LEVER;
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(key2, PersistentDataType.STRING)){
                String commandOnClick = meta.getPersistentDataContainer().get(key2, PersistentDataType.STRING);
                if (commandOnClick != null) {
                    System.out.println(commandOnClick);
                    Bukkit.dispatchCommand(event.getPlayer(), commandOnClick);
                }
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null && item.getItemMeta().getPersistentDataContainer().has(key4, PersistentDataType.BYTE)) {
                    if (!addItemOnClick(item) && !allowItemPlacement(clickedBlock.getType())) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean addItemOnClick(ItemStack itemStack) {

        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key4, PersistentDataType.BYTE)) {
                Byte addItemOnClickValue = itemMeta.getPersistentDataContainer().get(key4, PersistentDataType.BYTE);
                System.out.println("addItemOnClick " + (addItemOnClickValue != null && addItemOnClickValue == 1));
                return (addItemOnClickValue != null && addItemOnClickValue == 1);
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (!canDropItem(droppedItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!changeHand) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int slotClicked = event.getHotbarButton();
        if (!changeOnSlot(player, clickedItem, slotClicked) || !changeOnSlot(player, cursorItem, slotClicked)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    private boolean changeOnSlot(Player player , ItemStack itemStack, int slot) {
        if (slot >= 0 && slot < player.getInventory().getSize()) {
            PlayerInventory inventory = player.getInventory();
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.hasItemMeta()) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE)) {
                    Byte changeSlotValue = itemMeta.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
                    System.out.println("changeOnSlot "+ (changeSlotValue != null && changeSlotValue == 1));
                    return (changeSlotValue != null && changeSlotValue == 1);
                }
            }
        }
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE)) {
                Byte changeSlotValue = itemMeta.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
                System.out.println("changeOnSlot "+ (changeSlotValue != null && changeSlotValue == 1));
                return (changeSlotValue != null && changeSlotValue == 1);
            }
        }
        return true;
    }

    private boolean canDropItem(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key1, PersistentDataType.BYTE)) {
                Byte setDropValue = itemMeta.getPersistentDataContainer().get(key1, PersistentDataType.BYTE);
                System.out.println("canDropItem: " + (setDropValue != null && setDropValue == 1));
                return setDropValue != null && setDropValue == 1;
            }
        }
        return false;
    }

    private ItemStack createItemStackFromConfig(ConfigurationSection itemConfig) {
        String material = itemConfig.getString("material");
        int quantity = itemConfig.getInt("quantity");
        String name = itemConfig.getString("name");
        boolean setDrop = itemConfig.getBoolean("set_drop");
        String commandOnClick = itemConfig.getString("command_onclick");
        boolean changeSlot = itemConfig.getBoolean("change_slot");
        boolean addItemOnClick = itemConfig.getBoolean("add_item_on_click");
        List<String> loreList = itemConfig.getStringList("lore");

        ItemStack itemStack = new ItemStack(Material.valueOf(material), quantity);
        ItemMeta meta = itemStack.getItemMeta();

        if (name != null && !name.isEmpty()) {
            LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                    .character('&')
                    .build();
            Component displayName = serializer.deserialize(name);
            displayName = displayName.decoration(TextDecoration.ITALIC, false);
            meta.displayName(displayName);
        }

        if (!loreList.isEmpty()) {
            List<Component> lore = new ArrayList<>();
            LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                    .character('&')
                    .build();
            for (String loreLine : loreList) {
                Component formattedLoreLine = serializer.deserialize(loreLine);
                formattedLoreLine = formattedLoreLine.decoration(TextDecoration.ITALIC, false);
                lore.add(formattedLoreLine);
            }
            meta.lore(lore);
        }
        meta.getPersistentDataContainer().set(key1, PersistentDataType.BYTE, setDrop ? (byte) 1 : (byte) 0);
        if (commandOnClick != null && !commandOnClick.isEmpty()) {
            meta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, commandOnClick);
        }
        meta.getPersistentDataContainer().set(key3, PersistentDataType.BYTE, changeSlot ? (byte) 1 : (byte) 0);
        meta.getPersistentDataContainer().set(key4, PersistentDataType.BYTE, addItemOnClick ? (byte) 1 : (byte) 0);

        itemStack.setItemMeta(meta);
        return itemStack;
    }



    public void updatePlayerInventories() {
        List<Player> onlinePlayers = new ArrayList<>(server.getOnlinePlayers());
        if (clearInventory) {
            onlinePlayers.forEach(player -> player.getInventory().clear());
        }
        onlinePlayers.forEach(player -> {
            if (config.contains("items")) {
                ConfigurationSection itemsConfig = config.getConfigurationSection("items");
                if (itemsConfig != null) {
                    itemsConfig.getKeys(false).forEach(itemName -> {
                        ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                        if (itemConfig != null) {
                            ItemStack itemStack = createItemStackFromConfig(itemConfig);
                            int slot = itemConfig.getInt("slot");
                            if (config.getBoolean("delete_duplicate_meta_items")) {
                                ItemStack[] contents = player.getInventory().getContents();
                                for (int i = 0; i < contents.length; i++) {
                                    ItemStack currentItem = contents[i];
                                    if (currentItem != null && currentItem.isSimilar(itemStack) && i != slot) {
                                        System.out.println("Slot: " + i + ", Item: " + currentItem);
                                        player.getInventory().setItem(i, null);
                                    }
                                }
                            }
                            player.getInventory().setItem(slot, itemStack);
                        }
                    });
                }
            }
        });
    }



}
