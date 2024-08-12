package org.minecrafttest.main.Version.ItemsAsign;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.minecrafttest.main.Version.MetadataBuilder;

public class ModernMetadataBuilder implements MetadataBuilder {

    private JavaPlugin plugin;

    @Override
    public MetadataBuilder getPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        return this;
    }

    @Override
    public <T> void setCustomData(ItemStack item, String key, @NotNull T value) {
        ItemMeta meta = item.getItemMeta();

        NamespacedKey thisKey = new NamespacedKey(plugin, key);

        if (meta == null) return;

        if (value instanceof Integer) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.INTEGER, (Integer) value);
        } else if (value instanceof String) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.STRING, (String) value);
        } else if (value instanceof Byte) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.BYTE, (Byte) value);
        } else if (value instanceof Double) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.DOUBLE, (Double) value);
        } else if (value instanceof Long) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.LONG, (Long) value);
        } else if (value instanceof Float) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.FLOAT, (Float) value);
        } else if (value instanceof Short) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.SHORT, (Short) value);
        } else if (value instanceof Boolean) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.BYTE, (byte) ((Boolean) value ? 1 : 0));
        } else if (value instanceof byte[]) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.BYTE_ARRAY, (byte[]) value);
        } else if (value instanceof int[]) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.INTEGER_ARRAY, (int[]) value);
        } else if (value instanceof long[]) {
            meta.getPersistentDataContainer().set(thisKey, PersistentDataType.LONG_ARRAY, (long[]) value);
        } else {
            throw new IllegalArgumentException("Unsupported data type");
        }

        item.setItemMeta(meta);
    }

    @Override
    public <T> boolean hasCustomData(ItemStack item, String key, Class<T> clazz) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey thisKey = new NamespacedKey(plugin, key);

        if (meta == null) return false;

        if (clazz.equals(Integer.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.INTEGER);
        } else if (clazz.equals(String.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.STRING);
        } else if (clazz.equals(Byte.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.BYTE);
        } else if (clazz.equals(Double.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.DOUBLE);
        } else if (clazz.equals(Long.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.LONG);
        } else if (clazz.equals(Float.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.FLOAT);
        } else if (clazz.equals(Short.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.SHORT);
        } else if (clazz.equals(Boolean.class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.BYTE);
        } else if (clazz.equals(byte[].class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.BYTE_ARRAY);
        } else if (clazz.equals(int[].class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.INTEGER_ARRAY);
        } else if (clazz.equals(long[].class)) {
            return meta.getPersistentDataContainer().has(thisKey, PersistentDataType.LONG_ARRAY);
        } else {
            throw new IllegalArgumentException("Unsupported data type");
        }
    }

    @Override
    public <T> T getCustomData(ItemStack item, String key, Class<T> clazz) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey thisKey = new NamespacedKey(plugin, key);

        if (meta == null) return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (clazz.equals(Integer.class)) {
            return container.has(thisKey, PersistentDataType.INTEGER) ? clazz.cast(container.get(thisKey, PersistentDataType.INTEGER)) : null;
        } else if (clazz.equals(String.class)) {
            return container.has(thisKey, PersistentDataType.STRING) ? clazz.cast(container.get(thisKey, PersistentDataType.STRING)) : null;
        } else if (clazz.equals(Byte.class)) {
            return container.has(thisKey, PersistentDataType.BYTE) ? clazz.cast(container.get(thisKey, PersistentDataType.BYTE)) : null;
        } else if (clazz.equals(Double.class)) {
            return container.has(thisKey, PersistentDataType.DOUBLE) ? clazz.cast(container.get(thisKey, PersistentDataType.DOUBLE)) : null;
        } else if (clazz.equals(Long.class)) {
            return container.has(thisKey, PersistentDataType.LONG) ? clazz.cast(container.get(thisKey, PersistentDataType.LONG)) : null;
        } else if (clazz.equals(Float.class)) {
            return container.has(thisKey, PersistentDataType.FLOAT) ? clazz.cast(container.get(thisKey, PersistentDataType.FLOAT)) : null;
        } else if (clazz.equals(Short.class)) {
            return container.has(thisKey, PersistentDataType.SHORT) ? clazz.cast(container.get(thisKey, PersistentDataType.SHORT)) : null;
        } else if (clazz.equals(Boolean.class)) {
            return container.has(thisKey, PersistentDataType.BYTE) ? clazz.cast(container.get(thisKey, PersistentDataType.BOOLEAN)) : null;
        } else if (clazz.equals(byte[].class)) {
            return container.has(thisKey, PersistentDataType.BYTE_ARRAY) ? clazz.cast(container.get(thisKey, PersistentDataType.BYTE_ARRAY)) : null;
        } else if (clazz.equals(int[].class)) {
            return container.has(thisKey, PersistentDataType.INTEGER_ARRAY) ? clazz.cast(container.get(thisKey, PersistentDataType.INTEGER_ARRAY)) : null;
        } else if (clazz.equals(long[].class)) {
            return container.has(thisKey, PersistentDataType.LONG_ARRAY) ? clazz.cast(container.get(thisKey, PersistentDataType.LONG_ARRAY)) : null;
        } else {
            throw new IllegalArgumentException("Unsupported data type");
        }
    }
}