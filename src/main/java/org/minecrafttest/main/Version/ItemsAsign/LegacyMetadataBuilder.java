package org.minecrafttest.main.Version.ItemsAsign;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.minecrafttest.main.Version.MetadataBuilder;

@SuppressWarnings("deprecation")
public class LegacyMetadataBuilder implements MetadataBuilder {

    @Override
    public MetadataBuilder getPlugin(JavaPlugin plugin) {
        return this;
    }

    @Override
    public <T> void setCustomData(ItemStack item, String key, @NotNull T value) {
        NBTItem nbtItem = new NBTItem(item);
        if (value instanceof Integer) {
            nbtItem.setInteger(key, (Integer) value);
        } else if (value instanceof String) {
            nbtItem.setString(key, (String) value);
        } else if (value instanceof Byte) {
            nbtItem.setByte(key, (Byte) value);
        } else if (value instanceof Double) {
            nbtItem.setDouble(key, (Double) value);
        } else if (value instanceof Long) {
            nbtItem.setLong(key, (Long) value);
        } else if (value instanceof Float) {
            nbtItem.setFloat(key, (Float) value);
        } else if (value instanceof Short) {
            nbtItem.setShort(key, (Short) value);
        } else if (value instanceof Boolean) {
            nbtItem.setBoolean(key, (Boolean) value);
        } else if (value instanceof byte[]) {
            nbtItem.setByteArray(key, (byte[]) value);
        } else if (value instanceof int[]) {
            nbtItem.setIntArray(key, (int[]) value);
        } else if (value instanceof long[]) {
            nbtItem.setLongArray(key, (long[]) value);
        } else {
            throw new IllegalArgumentException("Unsupported data type");
        }
        nbtItem.applyNBT(item);
    }

    @Override
    public <T> boolean hasCustomData(ItemStack item, String key, Class<T> clazz) {
        NBTItem nbtItem = new NBTItem(item);

        if (!nbtItem.hasKey(key)) {
            return false;
        }
        try {
            if (clazz == Integer.class) {
                nbtItem.getInteger(key);
            } else if (clazz == String.class) {
                nbtItem.getString(key);
            } else if (clazz == Byte.class) {
                nbtItem.getByte(key);
            } else if (clazz == Double.class) {
                nbtItem.getDouble(key);
            } else if (clazz == Long.class) {
                nbtItem.getLong(key);
            } else if (clazz == Float.class) {
                nbtItem.getFloat(key);
            } else if (clazz == Short.class) {
                nbtItem.getShort(key);
            } else if (clazz == Boolean.class) {
                nbtItem.getBoolean(key);
            } else if (clazz == byte[].class) {
                nbtItem.getByteArray(key);
            } else if (clazz == int[].class) {
                nbtItem.getIntArray(key);
            } else if (clazz == long[].class) {
                nbtItem.getLongArray(key);
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public <T> T getCustomData(ItemStack item, String key, Class<T> clazz) {
        NBTItem nbtItem = new NBTItem(item);
        if (clazz == Integer.class) {
            return clazz.cast(nbtItem.getInteger(key));
        } else if (clazz == String.class) {
            return clazz.cast(nbtItem.getString(key));
        } else if (clazz == Byte.class) {
            return clazz.cast(nbtItem.getByte(key));
        } else if (clazz == Double.class) {
            return clazz.cast(nbtItem.getDouble(key));
        } else if (clazz == Long.class) {
            return clazz.cast(nbtItem.getLong(key));
        } else if (clazz == Float.class) {
            return clazz.cast(nbtItem.getFloat(key));
        } else if (clazz == Short.class) {
            return clazz.cast(nbtItem.getShort(key));
        } else if (clazz == Boolean.class) {
            return clazz.cast(nbtItem.getBoolean(key));
        } else if (clazz == byte[].class) {
            return clazz.cast(nbtItem.getByteArray(key));
        } else if (clazz == int[].class) {
            return clazz.cast(nbtItem.getIntArray(key));
        } else if (clazz == long[].class) {
            return clazz.cast(nbtItem.getLongArray(key));
        } else {
            return null;
        }
    }
}
