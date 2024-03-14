package org.minecrafttest.main.Listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.minecrafttest.main.ItemHandler;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class PlayerInteractionListener implements Listener {
    //PlayerInteractionListener
    private final ItemHandler plugin = ItemHandler.getPlugin();

    private final NamespacedKey key1 = new NamespacedKey(plugin, "set_drop");
    private final NamespacedKey key2_all = new NamespacedKey(plugin, "command_onclick");
    private final NamespacedKey key2_Left = new NamespacedKey(plugin, "command_left");
    private final NamespacedKey key2_Right = new NamespacedKey(plugin, "command_right");
    private final NamespacedKey key3 = new NamespacedKey(plugin, "change_slot");
    private final NamespacedKey key4 = new NamespacedKey(plugin, "add_item_on_click");
    private final NamespacedKey key5 = new NamespacedKey(plugin, "shotBow");
    private final NamespacedKey key6 = new NamespacedKey(plugin, "wear_armor");
    private final NamespacedKey key7 = new NamespacedKey(plugin,"delete_item_on_death");

    private final List<MaterialMetadata> materialInfoList = new ArrayList<>();
    private final List<ScheduledTask> taskMap = new ArrayList<>();
    private final Map<Runnable, Long> MapTask = new HashMap<>();


    public void loadAllIntervalItems(){
        String executor = plugin.getExecutor().getConfigName();
        String configName = !Objects.equals(executor, "") ? "profiles/"+executor : "profiles/subConfig";
        File configFile = new File(plugin.getDataFolder(), configName + ".yml");
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        if (yamlConfig.getKeys(false).isEmpty()) {return;}
        if (yamlConfig.contains("items")) {
            ConfigurationSection itemsConfig = yamlConfig.getConfigurationSection("items");
            assert itemsConfig != null;
            for (String itemName : itemsConfig.getKeys(false)) {
                ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                assert itemConfig != null;
                String material = itemConfig.getString("material", "DIRT");
                String quantity = itemConfig.getString("quantity", "1");
                String name = itemConfig.getString("name","");
                boolean setDrop = itemConfig.getBoolean("set_drop",true);
                String commandOnClick = itemConfig.getString("command_onclick","");
                String commandRight = itemConfig.getString("command_right","");
                String commandLeft = itemConfig.getString("command_left","");
                boolean changeSlot = itemConfig.getBoolean("change_slot",true);
                boolean addItemOnClick = itemConfig.getBoolean("add_item_on_click",true);
                boolean deleteItemOnDeath = itemConfig.getBoolean("delete_item_on_death",false);

                Object loreObj = itemConfig.get("lore");
                List<String> loreList = new ArrayList<>();

                if (loreObj instanceof List) {
                    loreList = itemConfig.getStringList("lore");
                } else if (loreObj instanceof String) {
                    String lo = itemConfig.getString("lore");
                    if (lo != null && !lo.isEmpty()) {
                        if (lo.contains("\n")) {
                            loreList.addAll(Arrays.asList(lo.split("\\n")));
                        } else {
                            loreList.add(lo);
                        }
                    }
                } else {
                    loreList = new ArrayList<>();
                }

                boolean shotBow = itemConfig.getBoolean("shotBow",true);
                boolean wearArmor = itemConfig.getBoolean("wear_armor",true);

                long changeIntervalSeconds = 2000;
                if (itemConfig.contains("material_change_interval_seconds")) {
                    changeIntervalSeconds =  (1000 *(itemConfig.getLong("material_change_interval_seconds", 1000) <= 0? 1 : itemConfig.getLong("material_change_interval_seconds",1000)));
                }
                if (itemConfig.contains("material_change_interval_milliseconds")) {
                    changeIntervalSeconds = ((itemConfig.getLong("material_change_interval_milliseconds", 1000) <= 0? 1 : itemConfig.getLong("material_change_interval_milliseconds",1000)));
                }

                int slot = itemConfig.getInt("slot",0);


                if (itemConfig.contains("materials")) {
                    String item_Name = itemConfig.getName();
                    ConfigurationSection materialsConfig = itemConfig.getConfigurationSection("materials");
                    assert materialsConfig != null;
                    for (String materialName : materialsConfig.getKeys(false)) {

                        ConfigurationSection materialConfig = materialsConfig.getConfigurationSection(materialName);
                        assert materialConfig != null;
                        String subMaterial = materialConfig.getString("sub_material",material);
                        String  sub_quantity = materialConfig.getString("sub_quantity",quantity);
                        String sub_name = materialConfig.getString("sub_name",name);
                        String sub_command = materialConfig.getString("sub_command",commandOnClick);
                        String sub_commandRight = materialConfig.getString("sub_command_Right",commandRight);
                        String sub_commandLeft = materialConfig.getString("sub_command_Left",commandLeft);
                        boolean sub_add_Item = materialConfig.getBoolean("sub_add_item_on_click",addItemOnClick);
                        boolean sub_shotBow = materialConfig.getBoolean("shotBow",shotBow);
                        boolean sub_wearArmor = materialConfig.getBoolean("wear_armor",wearArmor);
                        boolean sub_deleteItemOnDeath = itemConfig.getBoolean("sub_delete_item_on_death",deleteItemOnDeath);
                        Map<String, String> enchantments = new HashMap<>();


                        List<String> sub_Lore = new ArrayList<>();
                        Object subLoreObj = materialConfig.get("sub_lore");
                        if (subLoreObj instanceof List) {
                            sub_Lore = materialConfig.getStringList("sub_lore");
                        } else if (subLoreObj instanceof String) {
                            String lo = materialConfig.getString("sub_lore");
                            if (lo != null && !lo.isEmpty()) {
                                if (lo.contains("\n")) {
                                    sub_Lore.addAll(Arrays.asList(lo.split("\\n")));
                                } else {
                                    sub_Lore.add(lo);
                                }
                            }
                        } else {
                            sub_Lore = new ArrayList<>();
                        }

                        sub_Lore.addAll(loreList);

                        if (itemConfig.contains("enchantments")) {
                            ConfigurationSection enchantmentsConfig = itemConfig.getConfigurationSection("enchantments");
                            assert enchantmentsConfig != null;
                            for (String enchantmentKey : enchantmentsConfig.getKeys(false)) {
                                if (enchantmentKey != null) {
                                    String levelString = enchantmentsConfig.getString(enchantmentKey);
                                    enchantments.put(enchantmentKey, levelString);
                                }
                            }
                        }

                        if (materialConfig.contains("enchantments")) {
                            ConfigurationSection enchantmentsConfig = materialConfig.getConfigurationSection("enchantments");
                            assert enchantmentsConfig != null;
                            for (String enchantmentKey : enchantmentsConfig.getKeys(false)) {
                                if (enchantmentKey != null) {
                                    String levelString = enchantmentsConfig.getString(enchantmentKey);
                                    enchantments.put(enchantmentKey, levelString);
                                }
                            }
                        }

                        MaterialMetadata subMaterialInfo = new MaterialMetadata(subMaterial, sub_quantity, sub_name, sub_command,sub_commandRight, sub_commandLeft , sub_Lore, sub_add_Item, setDrop, changeSlot, item_Name, enchantments, sub_shotBow, sub_wearArmor, sub_deleteItemOnDeath);
                        materialInfoList.add(subMaterialInfo);
                    }
                    startMaterialChangeThread(changeIntervalSeconds, slot, item_Name);
                }
            }
        }
    }

    public void runThreads() {
        for (Map.Entry<Runnable, Long> entry : MapTask.entrySet()) {
            Runnable task = entry.getKey();
            long delay = entry.getValue();
            taskMap.add(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), 0, delay, TimeUnit.MILLISECONDS));
        }
    }
    public void cancelMaterialChangeTasks(boolean clear) {
        for (ScheduledTask task : taskMap) {
            task.cancel();
        }
        if (clear){
            taskMap.clear();
            MapTask.clear();
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setItems(player);
    }
    public void setItems(Player player) {
        if (plugin.getCustomConfig().getClearInventory()) {
            Inventory inventory = player.getInventory();
            ItemStack[] contents = inventory.getContents();
            for (ItemStack itemStack : contents) {
                if (itemStack != null && isItemFromPlugin(itemStack)) {
                    inventory.remove(itemStack);
                }
            }
        }
        String executor = plugin.getExecutor().getConfigName();
        String configName = !Objects.equals(executor, "") ? "profiles/"+executor : "profiles/subConfig";
        File configFile = new File(plugin.getDataFolder(), configName + ".yml");
        YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        if (yamlConfig.getKeys(false).isEmpty()) {return;}
        if (yamlConfig.contains("items")) {
            ConfigurationSection itemsConfig = yamlConfig.getConfigurationSection("items");
            assert itemsConfig != null;
            for (String itemName : itemsConfig.getKeys(false)) {
                ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                assert itemConfig != null;
                int slot = checksSlotException(itemConfig.getInt("slot"));
                ItemStack existingItem = player.getInventory().getItem(slot);
                if (existingItem == null || isItemFromPlugin(existingItem)) {
                    ItemStack newItem = createItemStackFromConfig(itemConfig, player);
                    player.getInventory().setItem(slot, newItem);
                }
            }
        }
    }
    private boolean isItemFromPlugin(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return true;

        return !meta.getPersistentDataContainer().has(key1, PersistentDataType.BYTE)
                || !meta.getPersistentDataContainer().has(key2_all, PersistentDataType.STRING)
                || !meta.getPersistentDataContainer().has(key2_Left, PersistentDataType.STRING)
                || !meta.getPersistentDataContainer().has(key2_Right, PersistentDataType.STRING)
                || !meta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE)
                || !meta.getPersistentDataContainer().has(key4, PersistentDataType.BYTE)
                || !meta.getPersistentDataContainer().has(key5, PersistentDataType.BYTE)
                || !meta.getPersistentDataContainer().has(key6, PersistentDataType.BYTE)
                || !meta.getPersistentDataContainer().has(key7, PersistentDataType.BYTE);
    }
    @SuppressWarnings("deprecation")
    private boolean allowItemPlacement(Material material) {
        return material == Material.CHEST
                || material.isInteractable()
                || material == Material.REDSTONE_TORCH
                || material == Material.LEVER;
    }
    private boolean shot(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key5, PersistentDataType.BYTE)) {
                Byte setDropValue = itemMeta.getPersistentDataContainer().get(key5, PersistentDataType.BYTE);
                return setDropValue != null && setDropValue != 1;
            }
        }
        return true;
    }

    private boolean wear(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key6, PersistentDataType.BYTE)) {
                Byte setDropValue = itemMeta.getPersistentDataContainer().get(key6, PersistentDataType.BYTE);
                return setDropValue != null && setDropValue != 1;
            }
        }
        return true;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {



        ItemStack item = event.getItem();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            if (meta != null && meta.getPersistentDataContainer().has(key5, PersistentDataType.BYTE)){
                if (shot(item)){
                    if (clickedBlock != null){
                        if (allowItemPlacement(clickedBlock.getType())){
                            return;
                        }
                    }
                    event.setCancelled(true);
                }
            }

            if (meta != null && meta.getPersistentDataContainer().has(key6, PersistentDataType.BYTE)){
                if (wear(item)){
                    if (clickedBlock != null){
                        if (allowItemPlacement(clickedBlock.getType())){
                            return;
                        }
                    }
                    event.setCancelled(true);
                    player.updateInventory();
                }
            }

            if (meta != null && meta.getPersistentDataContainer().has(key2_all, PersistentDataType.STRING)){
                String commandOnClick = meta.getPersistentDataContainer().get(key2_all, PersistentDataType.STRING);
                if (commandOnClick != null) {
                    Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, commandOnClick));
                }
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (clickedBlock != null && item.getItemMeta().getPersistentDataContainer().has(key4, PersistentDataType.BYTE)) {
                    if (!addItemOnClick(item) && !allowItemPlacement(clickedBlock.getType())) {
                        event.setCancelled(true);
                    }
                }
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (meta != null && meta.getPersistentDataContainer().has(key2_Right, PersistentDataType.STRING)) {
                    String commandOnClick = meta.getPersistentDataContainer().get(key2_Right, PersistentDataType.STRING);
                    if (commandOnClick != null) {
                        Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, commandOnClick));
                    }
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                if (meta != null && meta.getPersistentDataContainer().has(key2_Left, PersistentDataType.STRING)) {
                    String commandOnClick = meta.getPersistentDataContainer().get(key2_Left, PersistentDataType.STRING);
                    if (commandOnClick != null) {
                        Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, commandOnClick));
                    }
                }
            }
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player eventPlayer = event.getPlayer();
        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> setItems(eventPlayer), 250, TimeUnit.MILLISECONDS);

        for (ItemStack drop : event.getDrops()) {
            if (drop != null && drop.hasItemMeta()) {
                ItemMeta meta = drop.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(key7, PersistentDataType.BYTE)) {
                    Byte deleteItemOnDeath = meta.getPersistentDataContainer().get(key7, PersistentDataType.BYTE);
                    if (deleteItemOnDeath != null && deleteItemOnDeath == 1) {
                        drop.setAmount(0);
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
                return (addItemOnClickValue != null && addItemOnClickValue == 1);
            }
        }
        return true;
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (canDropItem(droppedItem)&& droppedItem.getItemMeta().getPersistentDataContainer().has(key1)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!plugin.getCustomConfig().getChangeHand()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int slotClicked = event.getHotbarButton();
        InventoryAction action = event.getAction();
        if (action == InventoryAction.SWAP_WITH_CURSOR) {
            assert clickedItem != null;
            ItemMeta itemMeta1 = clickedItem.getItemMeta();
            ItemMeta itemMeta2 = cursorItem.getItemMeta();
            Byte Change1 = itemMeta1.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
            Byte Change2 = itemMeta2.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
            if (!Objects.equals(Change1, Change2)){
                event.setCancelled(true);
                player.updateInventory();
            }
        }
        if (changeOnSlot(player, clickedItem, slotClicked) || changeOnSlot(player, cursorItem, slotClicked)) {
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
                    return (changeSlotValue != null && changeSlotValue == 0);
                }
            }
        }
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE)) {
                Byte changeSlotValue = itemMeta.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
                return (changeSlotValue != null && changeSlotValue == 0);
            }
        }
        return false;
    }

    private boolean canDropItem(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key1, PersistentDataType.BYTE)) {
                Byte setDropValue = itemMeta.getPersistentDataContainer().get(key1, PersistentDataType.BYTE);
                return setDropValue != null && setDropValue == 0;
            }
        }
        return true;
    }

    private ItemStack createItemStackFromConfig(ConfigurationSection itemConfig, Player player) {
        String material = itemConfig.getString("material", "DIRT");

        String materialMeta = PlaceholderAPI.setPlaceholders(player, material);
        Material mat = Material.matchMaterial(materialMeta.toUpperCase());

        if (mat == null) {
            Bukkit.getLogger().warning("Invalid material name: " + materialMeta);
        } else {
            String quantity = itemConfig.getString("quantity", "1");
            String name = itemConfig.getString("name","");
            boolean setDrop = itemConfig.getBoolean("set_drop", true);
            String commandOnClick = itemConfig.getString("command_onclick","");
            String commandRight = itemConfig.getString("command_right","");
            String commandLeft = itemConfig.getString("command_left","");
            boolean changeSlot = itemConfig.getBoolean("change_slot",true);
            boolean addItemOnClick = itemConfig.getBoolean("add_item_on_click",true);
            boolean shotBow = itemConfig.getBoolean("shotBow",true);
            boolean wearArmor = itemConfig.getBoolean("wear_armor",true);
            boolean deleteItemOnDeath = itemConfig.getBoolean("delete_item_on_death",false);


            Object loreObj = itemConfig.get("lore");
            List<String> loreList = new ArrayList<>();

            if (loreObj instanceof List) {
                loreList = itemConfig.getStringList("lore");
            } else if (loreObj instanceof String) {
                String lo = itemConfig.getString("lore");
                if (lo != null && !lo.isEmpty()) {
                    if (lo.contains("\n")) {
                        loreList.addAll(Arrays.asList(lo.split("\\n")));
                    } else {
                        loreList.add(lo);
                    }
                }
            } else {
                loreList = new ArrayList<>();
            }

            String quantityString = PlaceholderAPI.setPlaceholders(player, quantity);
            int real_quantity;
            try {
                real_quantity = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                real_quantity = 1;
                LogRecord logRecord = new LogRecord(Level.SEVERE, "Failed to parse quantity: " + e.getMessage());
                logRecord.setThrown(e);
                Bukkit.getLogger().log(logRecord);
            }

            ItemStack itemStack = new ItemStack(mat, real_quantity);
            ItemMeta meta = itemStack.getItemMeta();

            if (itemConfig.contains("enchantments")) {
                ConfigurationSection enchantmentsConfig = itemConfig.getConfigurationSection("enchantments");
                assert enchantmentsConfig != null;
                for (String enchantmentKey : enchantmentsConfig.getKeys(false)) {
                    NamespacedKey key = new NamespacedKey("minecraft", PlaceholderAPI.setPlaceholders(player, enchantmentKey));
                    Enchantment enchantment = Registry.ENCHANTMENT.get(key);
                    if (enchantment != null) {
                        int level;
                        try {
                            level = Integer.parseInt(PlaceholderAPI.setPlaceholders(player, Objects.requireNonNull(enchantmentsConfig.getString(enchantmentKey))));
                        } catch (NumberFormatException e) {
                            level = 1;
                            LogRecord logRecord = new LogRecord(Level.SEVERE, "Failed to parse quantity: " + e.getMessage());
                            logRecord.setThrown(e);
                            Bukkit.getLogger().log(logRecord);
                        }
                        meta.addEnchant(enchantment, level, true);
                    } else {
                        Component enableMessage = Component.text()
                                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                                .append(Component.text("Error: ", NamedTextColor.RED))
                                .append(Component.text("Enchantment " + enchantmentKey + " not found ", NamedTextColor.RED))
                                .build();
                        Bukkit.getConsoleSender().sendMessage(enableMessage);
                    }
                }
            }

            if (!name.isEmpty()) {
                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                        .character('&')
                        .build();
                Component displayName = serializer.deserialize(PlaceholderAPI.setPlaceholders(player, name));
                displayName = displayName.decoration(TextDecoration.ITALIC, false);
                meta.displayName(displayName);
            }

            if (!loreList.isEmpty()) {
                List<Component> lore = new ArrayList<>();
                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                        .character('&')
                        .build();
                for (String loreLine : loreList) {
                    Component formattedLoreLine = serializer.deserialize(PlaceholderAPI.setPlaceholders(player, loreLine));
                    formattedLoreLine = formattedLoreLine.decoration(TextDecoration.ITALIC, false);
                    lore.add(formattedLoreLine);
                }
                meta.lore(lore);
            }

            meta.getPersistentDataContainer().set(key1, PersistentDataType.BYTE, setDrop ? (byte) 1 : (byte) 0);

            if (!Objects.requireNonNull(commandOnClick).isEmpty()) {
                meta.getPersistentDataContainer().set(key2_all, PersistentDataType.STRING, commandOnClick);
            }


            if (!Objects.requireNonNull(commandLeft).isEmpty()){
                meta.getPersistentDataContainer().set(key2_Left, PersistentDataType.STRING, commandLeft);
            }

            if (!Objects.requireNonNull(commandRight).isEmpty()){
                meta.getPersistentDataContainer().set(key2_Right, PersistentDataType.STRING, commandRight);
            }

            meta.getPersistentDataContainer().set(key3, PersistentDataType.BYTE, changeSlot ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key4, PersistentDataType.BYTE, addItemOnClick ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key5, PersistentDataType.BYTE, shotBow ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key6, PersistentDataType.BYTE, wearArmor ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key7, PersistentDataType.BYTE, deleteItemOnDeath ? (byte) 1 : (byte) 0);
            itemStack.setItemMeta(meta);
            return itemStack;
        }
        return new ItemStack(Material.DIRT, 1);
    }

    private void startMaterialChangeThread(long changeIntervalSeconds, int slot, String itemName) {
        AtomicInteger currentIndex = new AtomicInteger();
        MapTask.put(()->{
            if (!materialInfoList.isEmpty()) {
                List<MaterialMetadata> materialsForItem = materialInfoList.stream()
                        .filter(materialMetadata -> materialMetadata.getItemName().equals(itemName))
                        .collect(Collectors.toList());
                if (!materialsForItem.isEmpty()) {
                    currentIndex.updateAndGet(v -> v % materialsForItem.size());
                    MaterialMetadata nextMaterial = materialsForItem.get(currentIndex.get());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        ItemStack nextItemStack = createItemStackFromMaterialInfo(nextMaterial, player);
                        player.getInventory().setItem(slot, nextItemStack);
                    }
                    currentIndex.set((currentIndex.get() + 1) % materialsForItem.size());
                }
            }
        },changeIntervalSeconds);
    }

    private ItemStack createItemStackFromMaterialInfo(MaterialMetadata materialMetadata, Player player) {
        String materialMeta = PlaceholderAPI.setPlaceholders(player, materialMetadata.getMaterial());
        Material mat = Material.matchMaterial(materialMeta.toUpperCase());
        if (mat == null) {
            Bukkit.getLogger().warning("Invalid material name: " + materialMeta);
        } else {
            String quantityString = PlaceholderAPI.setPlaceholders(player, materialMetadata.getQuantity());
            int quantity;
            try {
                quantity = Integer.parseInt(quantityString);
            } catch (NumberFormatException e) {
                quantity = 1;
                LogRecord logRecord = new LogRecord(Level.SEVERE, "Failed to parse quantity: " + e.getMessage());
                logRecord.setThrown(e);
                Bukkit.getLogger().log(logRecord);
            }
            ItemStack itemStack = new ItemStack(mat, quantity);
            ItemMeta meta = itemStack.getItemMeta();


            if (!materialMetadata.getName().isEmpty()) {
                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                        .character('&')
                        .build();
                Component displayName = serializer.deserialize(PlaceholderAPI.setPlaceholders(player,materialMetadata.getName()));
                displayName = displayName.decoration(TextDecoration.ITALIC, false);
                meta.displayName(displayName);
            }

            if (!materialMetadata.getSubLore().isEmpty()) {
                List<Component> lore = new ArrayList<>();
                LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
                        .character('&')
                        .build();
                for (String loreLine : materialMetadata.getSubLore()) {
                    Component formattedLoreLine = serializer.deserialize(PlaceholderAPI.setPlaceholders(player, loreLine));
                    formattedLoreLine = formattedLoreLine.decoration(TextDecoration.ITALIC, false);
                    lore.add(formattedLoreLine);
                }
                meta.lore(lore);
            }

            if (!materialMetadata.getEnchantments().isEmpty()) {
                for (String enchantmentKey : materialMetadata.getEnchantments().keySet()) {
                    NamespacedKey key = new NamespacedKey("minecraft", PlaceholderAPI.setPlaceholders(player,enchantmentKey));
                    Enchantment enchantment = Registry.ENCHANTMENT.get(key);
                    if (enchantment != null) {
                        int level;
                        try {
                            level = Integer.parseInt(Objects.requireNonNull(PlaceholderAPI.setPlaceholders(player, materialMetadata.getEnchantments().get(enchantmentKey))));
                        } catch (NumberFormatException e) {
                            level = 1;
                            LogRecord logRecord = new LogRecord(Level.SEVERE, "Failed to parse quantity: " + e.getMessage());
                            logRecord.setThrown(e);
                            Bukkit.getLogger().log(logRecord);
                        }
                        meta.addEnchant(enchantment, level, true);
                    } else {
                        Component enableMessage = Component.text()
                                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                                .append(Component.text("Error: ", NamedTextColor.RED))
                                .append(Component.text("Enchantment " + enchantmentKey + " not found ", NamedTextColor.RED))
                                .build();
                        Bukkit.getConsoleSender().sendMessage(enableMessage);
                    }
                }
            }

            meta.getPersistentDataContainer().set(key1, PersistentDataType.BYTE, materialMetadata.isSetDrop() ? (byte) 1 : (byte) 0);
            if (!materialMetadata.getSubcommand().isEmpty()) {
                meta.getPersistentDataContainer().set(key2_all, PersistentDataType.STRING, materialMetadata.getSubcommand());
            }
            if (!materialMetadata.getSubcommandLeft().isEmpty()) {
                meta.getPersistentDataContainer().set(key2_Left, PersistentDataType.STRING, materialMetadata.getSubcommandLeft());
            }
            if (!materialMetadata.getSubcommandRight().isEmpty()) {
                meta.getPersistentDataContainer().set(key2_Right, PersistentDataType.STRING, materialMetadata.getSubcommandRight());
            }
            meta.getPersistentDataContainer().set(key3, PersistentDataType.BYTE, materialMetadata.isChangeSlot() ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key4, PersistentDataType.BYTE, materialMetadata.isAddItemOnClick() ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key5, PersistentDataType.BYTE, materialMetadata.shotBow() ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key6, PersistentDataType.BYTE, materialMetadata.wearArmor() ? (byte) 1 : (byte) 0);
            meta.getPersistentDataContainer().set(key7, PersistentDataType.BYTE, materialMetadata.deleteItemOnDeath() ? (byte) 1 : (byte) 0);
            itemStack.setItemMeta(meta);
            return itemStack;

        }
        return new ItemStack(Material.DIRT, 1);
    }

    public void updatePlayerInventories(String executor) {
        cancelMaterialChangeTasks(true);
        materialInfoList.clear();
        List<Player> onlinePlayers = new ArrayList<>(ItemHandler.getPlugin().getServer().getOnlinePlayers());
        if (plugin.getCustomConfig().getClearInventory()) {onlinePlayers.forEach(player -> player.getInventory().clear());}
        onlinePlayers.forEach(player -> {
            String configName = !Objects.equals(executor, "") ? executor : "subConfig";
            File configFile = new File(plugin.getDataFolder(), configName + ".yml");
            YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
            if (yamlConfig.getKeys(false).isEmpty()) {return;}
            if (yamlConfig.contains("items")) {
                ConfigurationSection itemsConfig = yamlConfig.getConfigurationSection("items");
                if (itemsConfig != null) {
                    itemsConfig.getKeys(false).forEach(itemName -> {
                        ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                        if (itemConfig != null) {
                            ItemStack itemStack = createItemStackFromConfig(itemConfig, player);
                            int slot = checksSlotException(itemConfig.getInt("slot",0));
                            if (plugin.getCustomConfig().getDeleteDuplicateMetaItems()) {
                                ItemStack[] contents = player.getInventory().getContents();
                                for (int i = 0; i < contents.length; i++) {
                                    ItemStack currentItem = contents[i];
                                    if (currentItem != null && currentItem.isSimilar(itemStack) && i != slot) {
                                        //System.out.println("Slot: " + i + ", Item: " + currentItem);
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
        loadAllIntervalItems();
        runThreads();
    }

    private int checksSlotException(int slot){
        if (slot < 0) {
            Component enableMessage = Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                    .append(Component.text("Error: ", NamedTextColor.RED))
                    .append(Component.text("Exception negative the number of slot = " + slot , NamedTextColor.RED))
                    //.append(Component.text("                                                                                           ^", NamedTextColor.RED))
                    .build();
            Bukkit.getConsoleSender().sendMessage(enableMessage);
            return 0;
        }
        if (slot > 35) {
            Component enableMessage = Component.text()
                    .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.RED))
                    .append(Component.text("Error: ", NamedTextColor.RED))
                    .append(Component.text("Exception exceeded the number of slot = " + slot , NamedTextColor.RED))
                    //.append(Component.text("                                                                                           ^", NamedTextColor.RED))
                    .build();
            Bukkit.getConsoleSender().sendMessage(enableMessage);
            return 35;
        }
        return slot;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block placedBlock = event.getBlockPlaced();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){

    }

}