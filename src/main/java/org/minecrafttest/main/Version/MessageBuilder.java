package org.minecrafttest.main.Version;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.Component.LegacyMessageBuilder;
import org.minecrafttest.main.Version.Component.ModernMessageBuilder;

import java.util.List;

public interface MessageBuilder {

    MessageBuilder append(String text);
    MessageBuilder append(String text, ColorText colorText);
    void build();
    void BukkitSender();
    void senderMessage(CommandSender sender);
    void applyMeta(ItemMeta meta, String name, Player player);
    void applyLore(ItemMeta meta, List<String> loreList, Player player);
    void sendActionBar(Player player, String message, int red, int green, int blue);

    static MessageBuilder createMessageBuilder() {
        if (APICompatibility.isModernAPIComponent()) {
            return new ModernMessageBuilder();
        } else {
            return new LegacyMessageBuilder();
        }
    }
}