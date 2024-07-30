package org.minecrafttest.main.Version.Component;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.minecrafttest.main.Version.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

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

    @SuppressWarnings("deprecation")
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