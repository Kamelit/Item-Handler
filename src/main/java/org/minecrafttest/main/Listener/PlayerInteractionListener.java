package org.minecrafttest.main.Listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Particles.TypesAnimation;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

//Listener
public class PlayerInteractionListener implements Listener {
    //Only Class
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
    private final Map<String,List<MaterialMetadata>> materialInfoList = new HashMap<>();

    private final Map<String, Map<Runnable, Long>> MapTask = new HashMap<>();

    private YamlConfiguration yamlConfig;
    private String quantity, material, name, commandOnClick, commandRight, commandLeft;
    private boolean setDrop, changeSlot, addItemOnClick, deleteItemOnDeath, shotBow, wearArmor;

    //Menus
    public final String[] groupKeysMenusType = {"main", "parkour"};

    //All Class
    public final Map<String, List<ScheduledTask>> taskMap = new HashMap<>();
    public final Map<String, List<Object>> worldConfigInMemory = new HashMap<>();
    public final Map<String, List<Player>> playerInventory = new HashMap<>();
    public YamlConfiguration worldConfig;

    public void loadAllResources() {
        for (String key : groupKeysMenusType) {
            playerInventory.putIfAbsent(key, new ArrayList<>());
            taskMap.putIfAbsent(key, new ArrayList<>());
        }
        plugin.getParkour().loadCheckpoints();
        worldConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "blocks_events/world.yml"));
        plugin.getParkour().parkourConfiguration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"parkour/parkour.yml"));
        if (getConfigurationName()) return;

        for (String path : yamlConfig.getKeys(false)) {
            ConfigurationSection itemsConfig = yamlConfig.getConfigurationSection(path);
            if (itemsConfig == null) continue;

            for (String itemName : itemsConfig.getKeys(false)) {
                ConfigurationSection itemConfig = itemsConfig.getConfigurationSection(itemName);
                if (itemConfig == null) continue;

                getMeta(itemConfig);
                List<String> loreList = getStrings(itemConfig, itemConfig.get("lore"), new ArrayList<>());

                long changeIntervalMillis = getChangeInterval(itemConfig);

                int slot = itemConfig.getInt("slot", 0);

                if (itemConfig.contains("materials")) {
                    String item_Name = itemConfig.getName();
                    ConfigurationSection materialsConfig = itemConfig.getConfigurationSection("materials");
                    if (materialsConfig == null) continue;

                    for (String materialName : materialsConfig.getKeys(false)) {
                        ConfigurationSection materialConfig = materialsConfig.getConfigurationSection(materialName);
                        if (materialConfig == null) continue;

                        MaterialMetadata subMaterialInfo = extractMaterialMetadata(materialConfig, itemConfig, item_Name, loreList);
                        materialInfoList.computeIfAbsent(path, k -> new ArrayList<>()).add(subMaterialInfo);
                    }
                    startMaterialChangeThread(changeIntervalMillis, slot, item_Name, path);
                }
            }
        }
    }

    private long getChangeInterval(ConfigurationSection itemConfig) {
        long changeIntervalMillis = 2000;
        if (itemConfig.contains("material_change_interval_seconds")) {
            changeIntervalMillis = 1000 * Math.max(1, itemConfig.getLong("material_change_interval_seconds", 1000));
        }
        if (itemConfig.contains("material_change_interval_milliseconds")) {
            changeIntervalMillis = Math.max(1, itemConfig.getLong("material_change_interval_milliseconds", 1000));
        }
        return changeIntervalMillis;
    }

    private MaterialMetadata extractMaterialMetadata(ConfigurationSection materialConfig, ConfigurationSection itemConfig, String itemName, List<String> loreList) {
        String subMaterial = materialConfig.getString("sub_material", material);
        String subQuantity = materialConfig.getString("sub_quantity", quantity);
        String subName = materialConfig.getString("sub_name", name);
        String subCommand = materialConfig.getString("sub_command", commandOnClick);
        String subCommandRight = materialConfig.getString("sub_command_Right", commandRight);
        String subCommandLeft = materialConfig.getString("sub_command_Left", commandLeft);
        boolean subAddItem = materialConfig.getBoolean("sub_add_item_on_click", addItemOnClick);
        boolean subShotBow = materialConfig.getBoolean("shotBow", shotBow);
        boolean subWearArmor = materialConfig.getBoolean("wear_armor", wearArmor);
        boolean subDeleteItemOnDeath = itemConfig.getBoolean("sub_delete_item_on_death", deleteItemOnDeath);

        List<String> subLore = getStrings(materialConfig, materialConfig.get("sub_lore"), new ArrayList<>());
        subLore.addAll(loreList);

        Map<String, String> enchantments = new HashMap<>();
        enchantments(itemConfig, enchantments);
        enchantments(materialConfig, enchantments);

        return new MaterialMetadata(subMaterial, subQuantity, subName, subCommand, subCommandRight, subCommandLeft, subLore, subAddItem, setDrop, changeSlot, itemName, enchantments, subShotBow, subWearArmor, subDeleteItemOnDeath);
    }


    private void enchantments(ConfigurationSection materialConfig, Map<String, String> enchantments) {
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
    }

    private List<String> getStrings(ConfigurationSection itemConfig, Object loreObj, List<String> loreList) {
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
        return loreList;
    }

    private void getMeta(ConfigurationSection itemConfig) {
        material = itemConfig.getString("material", "DIRT");
        quantity = itemConfig.getString("quantity", "1");
        name = itemConfig.getString("name","");
        setDrop = itemConfig.getBoolean("set_drop",true);
        commandOnClick = itemConfig.getString("command_onclick","");
        commandRight = itemConfig.getString("command_right","");
        commandLeft = itemConfig.getString("command_left","");
        changeSlot = itemConfig.getBoolean("change_slot",true);
        addItemOnClick = itemConfig.getBoolean("add_item_on_click",true);
        deleteItemOnDeath = itemConfig.getBoolean("delete_item_on_death",false);
        shotBow = itemConfig.getBoolean("shotBow",true);
        wearArmor = itemConfig.getBoolean("wear_armor",true);
    }

    private boolean getConfigurationName() {
        String executor = plugin.getExecutor().getConfigName();
        String configName = !Objects.equals(executor, "") ? "profiles/"+executor : "profiles/subConfig";
        yamlConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configName + ".yml"));
        return yamlConfig.getKeys(false).isEmpty();
    }



    public void runThreads() {
        cancelThreadsInNotUse();

        for (Map.Entry<String, Map<Runnable, Long>> entryM : MapTask.entrySet()) {
            String key = entryM.getKey();
            if (taskMap.get(key).isEmpty()) {
                for (Map.Entry<Runnable, Long> entry : entryM.getValue().entrySet()) {
                    Runnable task = entry.getKey();
                    long delay = entry.getValue();
                    if (playerInventory.containsKey(key) && !playerInventory.get(key).isEmpty()) {
                        taskMap.get(key).add(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), 0, delay, TimeUnit.MILLISECONDS));
                    }
                }
            }
        }
    }

    public void cancelThreadsInNotUse() {
        for (String key : new ArrayList<>(playerInventory.keySet())) {
            List<Player> players = playerInventory.get(key);
            if (players.isEmpty()) {
                List<ScheduledTask> tasks = taskMap.get(key);
                if (tasks != null) {
                    for (ScheduledTask task : tasks) {
                        task.cancel();
                    }
                    tasks.clear();
                }
            }
        }
    }

    public void cancelAllMaterialChangeTasks(){
        for (List<ScheduledTask> tasks : taskMap.values()) {
            for (ScheduledTask task : tasks) {
                task.cancel();
            }
            tasks.clear();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerInventory.get(groupKeysMenusType[0]).add(player);
        System.out.println(playerInventory);
        setItems(player, groupKeysMenusType[0]);
        runThreads();
    }


    public void setItems(Player player, String path) {
        if (plugin.getCustomConfig().getClearInventory()) {
            Inventory inventory = player.getInventory();
            ItemStack[] contents = inventory.getContents();
            for (ItemStack itemStack : contents) {
                if (itemStack != null && isItemFromPlugin(itemStack)) {
                    inventory.remove(itemStack);
                }
            }
        }
        if (getConfigurationName()) return;
        if (yamlConfig.contains(path)) {
            ConfigurationSection itemsConfig = yamlConfig.getConfigurationSection(path);
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
        return CheckUse(itemStack, key5);
    }

    private boolean CheckUse(ItemStack itemStack, NamespacedKey key5) {
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
        return CheckUse(itemStack, key6);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player eventPlayer = event.getPlayer();
        Bukkit.getAsyncScheduler().runDelayed(plugin, scheduledTask -> setItems(eventPlayer, groupKeysMenusType[0]), 250, TimeUnit.MILLISECONDS);

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
            Boolean changeSlotValue = getaBooleanMeta(item);
            if (changeSlotValue != null) return changeSlotValue;
        }
        Boolean changeSlotValue = getaBooleanMeta(itemStack);
        if (changeSlotValue != null) return changeSlotValue;
        return false;
    }

    @Nullable
    private Boolean getaBooleanMeta(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null && itemMeta.getPersistentDataContainer().has(key3, PersistentDataType.BYTE)) {
                Byte changeSlotValue = itemMeta.getPersistentDataContainer().get(key3, PersistentDataType.BYTE);
                return (changeSlotValue != null && changeSlotValue == 0);
            }
        }
        return null;
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
        material = itemConfig.getString("material", "DIRT");

        String materialMeta = PlaceholderAPI.setPlaceholders(player, material);
        Material mat = Material.matchMaterial(materialMeta.toUpperCase());

        if (mat == null) {
            plugin.getLogger().warning("Invalid material name: " + materialMeta);
        } else {
            getMeta(itemConfig);
            Object loreObj = itemConfig.get("lore");
            List<String> loreList = new ArrayList<>();
            loreList = getStrings(itemConfig, loreObj, loreList);

            String quantityString = PlaceholderAPI.setPlaceholders(player, quantity);
            ItemStack itemStack = new ItemStack(mat, checkQuantity(quantityString));
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
                            plugin.getLogger().log(logRecord);
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

    private void startMaterialChangeThread(long changeIntervalSeconds, int slot, String itemName, String path) {
        AtomicInteger currentIndex = new AtomicInteger(0);
        Runnable runnable = () -> {
            if (!materialInfoList.isEmpty()) {
                List<MaterialMetadata> materialsForItem = materialInfoList.get(path).stream()
                        .filter(materialMetadata -> materialMetadata.getItemName().equals(itemName))
                        .collect(Collectors.toList());
                if (!materialsForItem.isEmpty()) {
                    currentIndex.getAndUpdate(v -> (v + 1) % materialsForItem.size());
                    MaterialMetadata nextMaterial = materialsForItem.get(currentIndex.get());

                    playerInventory.getOrDefault(path, Collections.emptyList())
                            .forEach(player -> {
                                ItemStack nextItemStack = createItemStackFromMaterialInfo(nextMaterial, player);
                                player.getInventory().setItem(slot, nextItemStack);
                            });
                }
            }
        };
        Map<Runnable, Long> tasks = MapTask.computeIfAbsent(path, k -> new HashMap<>());
        tasks.put(runnable, changeIntervalSeconds);
    }

    private int checkQuantity(String getQuantity){
        int real_quantity;
        try {
            real_quantity = Integer.parseInt(getQuantity);
        } catch (NumberFormatException e) {
            real_quantity = 1;
            LogRecord logRecord = new LogRecord(Level.SEVERE, "Failed to parse quantity: " + e.getMessage());
            logRecord.setThrown(e);
            plugin.getLogger().log(logRecord);
        }
        return real_quantity;
    }

    private ItemStack createItemStackFromMaterialInfo(MaterialMetadata materialMetadata, Player player) {
        String materialMeta = PlaceholderAPI.setPlaceholders(player, materialMetadata.getMaterial());
        Material mat = Material.matchMaterial(materialMeta.toUpperCase());
        if (mat == null) {
            plugin.getLogger().warning("Invalid material name: " + materialMeta);
        } else {
            String quantityString = PlaceholderAPI.setPlaceholders(player, materialMetadata.getQuantity());

            ItemStack itemStack = new ItemStack(mat, checkQuantity(quantityString));
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
                            plugin.getLogger().log(logRecord);
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

    public void updates(String executor) {
        cancelAllMaterialChangeTasks();
        MapTask.clear();
        plugin.getParticleAnimation().RemoveAllTaskingParticles();
        plugin.getParkour().ClearCheckpoints();
        materialInfoList.clear();
        worldConfigInMemory.clear();
        Component message = Component.text()
                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_AQUA))
                .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("All Events is removed! ", NamedTextColor.RED))
                .build();
        Bukkit.getConsoleSender().sendMessage(message);
        List<Player> onlinePlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (plugin.getCustomConfig().getClearInventory()) {onlinePlayers.forEach(player -> player.getInventory().clear());}
        onlinePlayers.forEach(player -> {
            String configName = !Objects.equals(executor, "") ? executor : "subConfig";
            yamlConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configName + ".yml"));
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
        loadAllResources();

        Bukkit.getOnlinePlayers().forEach(player -> {
            String key = findKeyByPlayer(playerInventory, player);
            if (key != null) {
                if (plugin.getChronometer().isRunChronometer(player)) plugin.getChronometer().stopChronometer(player);
                plugin.getChronometer().stopChronometer(player);
                setItems(player, key);
            }
        });
        runThreads();
        worldConfig.getKeys(false).forEach(eventName->{
            int CX = worldConfig.getInt(eventName+".chunkX");
            int CZ = worldConfig.getInt(eventName+".chunkZ");
            World world = Bukkit.getWorld(Objects.requireNonNull(worldConfig.getString(eventName+".world")));
            if (world != null && world.isChunkLoaded(CX, CZ)){
                double x = worldConfig.getDouble(eventName + ".x");
                double y = worldConfig.getDouble(eventName + ".y");
                double z = worldConfig.getDouble(eventName + ".z");
                Block block = world.getBlockAt(new Location(world, x, y, z));
                int cx = block.getChunk().getX();
                int cz = block.getChunk().getZ();
                List <String> command = worldConfig.getStringList(eventName + ".command");
                List<String> interaction = worldConfig.getStringList(eventName + ".action");
                List<?> metaAnimation = Objects.requireNonNull(worldConfig.getList(eventName+".animation"));
                String animation = metaAnimation.get(0).toString();
                int radius = Integer.parseInt(metaAnimation.get(1).toString());
                List<Object> locationData = new ArrayList<>();
                Collections.addAll(locationData, block, command, cx, cz, world, metaAnimation);
                locationData.addAll(interaction);
                worldConfigInMemory.put(eventName, locationData);
                plugin.getParticleAnimation().playAnimation(eventName, block, TypesAnimation.valueOf(animation), radius);
                System.out.println(worldConfigInMemory.get(eventName));
                printMessage(eventName, block, world, true);
            }
        });
    }

    public static String findKeyByPlayer(Map<String, List<Player>> playerInventory, Player player) {
        for (Map.Entry<String, List<Player>> entry : playerInventory.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return null;
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

    private void checkActions(Block block, String text, Player player) {
        for (List<Object> commandData : worldConfigInMemory.values()) {
            Block storedBlock = (Block) commandData.get(0);
            if (block.equals(storedBlock) && commandData.contains(text)) {
                if (player != null && player.isOnline() && player.getWorld().equals(block.getWorld())) {
                    Object obj = commandData.get(1);
                    List<String> commands = safaCastList(obj, String.class);
                    for (String command : commands){
                        plugin.getServer().dispatchCommand(player, command);
                    }
                }
            }
        }
    }

    private void checkActions(Block block, String text) {
        for (List<Object> commandData : worldConfigInMemory.values()) {
            Block storedBlock = (Block) commandData.get(0);
            if (block.equals(storedBlock) && commandData.contains(text)) {
                Object obj = commandData.get(1);
                Class<String> type = String.class;
                List<String> commands = safaCastList(obj, type);
                for (String command : commands){
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> safaCastList(Object obj, Class<T> tClass){
        if (obj instanceof List<?>){
            List<?> list = (List<?>) obj;
            for (Object element : list){
                if (!tClass.isInstance(element)){
                    return null;
                }
            }
            return (List<T>) list;
        }
        return null;
    }

    @EventHandler
    public void onBlockBurned(BlockBurnEvent event) {
        Block burnedBlock = event.getBlock();
        checkActions(burnedBlock, "on_block_burned");
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ItemStack item = event.getItem();

        //Blocks
        if (clickedBlock != null) {
            for (List<Object> commandData : worldConfigInMemory.values()) {
                Block block = (Block) commandData.get(0);
                if (clickedBlock.equals(block)) {
                    /*if (commandData.contains("on_block_state_player_change") && clickedBlock.getType().name().contains("PRESSURE_PLATE")) {
                        Powerable plate = (Powerable) clickedBlock.getBlockData();
                        if (plate.isPowered()) {
                            Object obj = commandData.get(1);
                            List<String> commands = safaCastList(obj, String.class);
                            for (String command : commands) {
                                plugin.getServer().dispatchCommand(player, command);
                            }
                        }
                    }*/
                    if ((commandData.contains("on_click") && event.getAction() == Action.LEFT_CLICK_BLOCK) ||
                            (commandData.contains("on_right_click") && event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
                            (commandData.contains("on_left_click") && event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                        Object obj = commandData.get(1);
                        List<String> commands = safaCastList(obj, String.class);
                        for (String command : commands){
                            plugin.getServer().dispatchCommand(player, command);
                        }

                    } else if ((commandData.contains("on_right_click") && event.getAction() == Action.RIGHT_CLICK_AIR) ||
                            (commandData.contains("on_left_click") && event.getAction() == Action.LEFT_CLICK_AIR)) {
                        Object obj = commandData.get(1);
                        List<String> commands = safaCastList(obj, String.class);
                        for (String command : commands){
                            plugin.getServer().dispatchCommand(player, command);
                        }
                    }
                }
            }
        }

        if (item != null) {
            ItemMeta meta = item.getItemMeta();

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
                    String[] commands = commandOnClick.split(",\\s*");
                    for (String command : commands) {
                        Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, command));
                    }
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
                        String[] commands = commandOnClick.split(",\\s*");
                        for (String command : commands) {
                            Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, command));
                        }
                    }
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                if (meta != null && meta.getPersistentDataContainer().has(key2_Left, PersistentDataType.STRING)) {
                    String commandOnClick = meta.getPersistentDataContainer().get(key2_Left, PersistentDataType.STRING);
                    if (commandOnClick != null) {
                        String[] commands = commandOnClick.split(",\\s*");
                        for (String command : commands) {
                            Bukkit.dispatchCommand(event.getPlayer(), PlaceholderAPI.setPlaceholders(player, command));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block blockUnderPlayer = player.getLocation().getBlock();

        if (blockUnderPlayer.getType().name().contains("PRESSURE_PLATE")) {
            for (List<Object> commandData : worldConfigInMemory.values()) {
                Block block = (Block) commandData.get(0);
                if (blockUnderPlayer.equals(block)) {
                    if (commandData.contains("on_block_state_player_change")) {
                        Powerable plate = (Powerable) blockUnderPlayer.getBlockData();
                        if (plate.isPowered()) {
                            Object obj = commandData.get(1);
                            List<String> commands = safaCastList(obj, String.class);
                            for (String command : commands) {
                                plugin.getServer().dispatchCommand(player, command);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        checkActions(block, "on_block_break", player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        checkActions(block, "on_block_place", player);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        checkActions(block, "on_block_explode");
    }

    @EventHandler
    public void onBlockDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        checkActions(block, "on_block_decay");
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        checkActions(block, "on_block_grow");
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        checkActions(block, "on_block_redstone_change");
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        World world = event.getWorld();

        worldConfig.getKeys(false).stream().filter(eventName -> {
            int CCX = worldConfig.getInt(eventName + ".chunkX");
            int CCZ = worldConfig.getInt(eventName + ".chunkZ");
            String wrd = worldConfig.getString(eventName + ".world");
            return chunkX == CCX && chunkZ == CCZ && world.getName().equals(wrd);
        }).forEach(eventName -> {
            double x = worldConfig.getDouble(eventName + ".x");
            double y = worldConfig.getDouble(eventName + ".y");
            double z = worldConfig.getDouble(eventName + ".z");
            Block block = world.getBlockAt(new Location(world, x, y, z));
            int cx = block.getChunk().getX();
            int cz = block.getChunk().getZ();
            List<String> command = worldConfig.getStringList(eventName + ".command");
            List<String> interaction = worldConfig.getStringList(eventName + ".action");
            List<?> animation = worldConfig.getList(eventName + ".animation");
            List<Object> locationData = new ArrayList<>();
            Collections.addAll(locationData, block, command, cx, cz, world, animation);
            locationData.addAll(interaction);
            worldConfigInMemory.put(eventName, locationData);
            System.out.println(worldConfigInMemory.get(eventName));
            printMessage(eventName, block, world, true);
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        World world = event.getWorld();
        worldConfigInMemory.entrySet().removeIf(entry -> {
            List<Object> values = entry.getValue();
            int CCX = (int) (values.get(2));
            int CCZ = (int) (values.get(3));
            World wrd = ((World) values.get(4));
            if (chunkX == CCX && chunkZ == CCZ && world.equals(wrd)) {
                String eventName = entry.getKey();
                plugin.getParticleAnimation().RemoveTaskingParticles(eventName);
                printMessage(eventName, ((Block) values.get(0)), wrd, false);
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerInventory.values().forEach(players -> players.remove(player));
        if (plugin.getChronometer().isRunChronometer(player)) plugin.getChronometer().stopChronometer(player);
        cancelThreadsInNotUse();
    }

    public void printMessage(String eventName, Block block, World world, boolean registered) {
        NamedTextColor color = registered ? NamedTextColor.GREEN : NamedTextColor.RED;
        String action = registered ? "Registered" : "Unregistered";
        Component message = Component.text()
                .append(Component.text("[" + plugin.getName() + "] ", NamedTextColor.DARK_AQUA))
                .append(Component.text("[OptimizerHandler] ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(action + " " + eventName + ":", color))
                .append(Component.text(" X: " + block.getX() + " Y: " + block.getY() + " Z: " + block.getZ(), NamedTextColor.WHITE))
                .append(Component.text(" [" + world.getName() + "] ", NamedTextColor.AQUA))
                .build();
        Bukkit.getConsoleSender().sendMessage(message);
    }
}