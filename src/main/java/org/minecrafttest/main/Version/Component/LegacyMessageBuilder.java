package org.minecrafttest.main.Version.Component;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.minecrafttest.main.Version.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyMessageBuilder implements MessageBuilder {
    private final List<String> components = new ArrayList<>();
    private final StringBuilder message = new StringBuilder();

    @Override
    public MessageBuilder append(String text) {
        components.add(convertColor(ColorText.WHITE) + text);
        return this;
    }

    @Override
    public MessageBuilder append(String text, ColorText colorText) {
        components.add(convertColor(colorText) + text);
        return this;
    }


    @Override
    public void build() {
        for (String component : components) {
            message.append(component);
        }
    }

    @Override
    public void BukkitSender(){
        Bukkit.getConsoleSender().sendMessage(message.toString());
    }

    @Override
    public void senderMessage(CommandSender sender){
        sender.sendMessage(message.toString());
    }

    @Override
    public void applyMeta(ItemMeta meta, String name, Player player) {
        if (name != null && !name.isEmpty()) {

            String formattedName = PlaceholderAPI.setPlaceholders(player, name);

            formattedName = ChatColor.translateAlternateColorCodes('&', formattedName);

            meta.setDisplayName(formattedName);
        }
    }

    @Override
    public void applyLore(ItemMeta meta, List<String> loreList, Player player) {
        List<String> lore = new ArrayList<>();
        for (String loreLine : loreList) {
            String formattedLoreLine = PlaceholderAPI.setPlaceholders(player, loreLine);
            formattedLoreLine = ChatColor.translateAlternateColorCodes('&', formattedLoreLine);
            lore.add(formattedLoreLine);
        }
        meta.setLore(lore);
    }

    @Override
    public void sendActionBar(Player player, String message, int red, int green, int blue) {
        String hexColor = String.format("#%02x%02x%02x", red, green, blue);
        String formattedMessage = net.md_5.bungee.api.ChatColor.of(hexColor) + message;
        player.sendActionBar(formattedMessage);  // Replace this with actual method for older versions
    }

    private ChatColor convertColor(ColorText colorText) {
        switch (colorText) {
            case BLACK: return ChatColor.BLACK;
            case DARK_BLUE: return ChatColor.DARK_BLUE;
            case DARK_GREEN: return ChatColor.DARK_GREEN;
            case DARK_AQUA: return ChatColor.DARK_AQUA;
            case DARK_RED: return ChatColor.DARK_RED;
            case DARK_PURPLE: return ChatColor.DARK_PURPLE;
            case GOLD: return ChatColor.GOLD;
            case GRAY: return ChatColor.GRAY;
            case DARK_GRAY: return ChatColor.DARK_GRAY;
            case BLUE: return ChatColor.BLUE;
            case GREEN: return ChatColor.GREEN;
            case AQUA: return ChatColor.AQUA;
            case RED: return ChatColor.RED;
            case LIGHT_PURPLE: return ChatColor.LIGHT_PURPLE;
            case YELLOW: return ChatColor.YELLOW;
            case WHITE: return ChatColor.WHITE;
            default: return ChatColor.WHITE;
        }
    }
}