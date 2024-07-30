package org.minecrafttest.main.Version.Component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.minecrafttest.main.Version.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModernMessageBuilder implements MessageBuilder {
    private final List<Component> components = new ArrayList<>();
    private Component component;

    @Override
    public MessageBuilder append(String text) {
        components.add(Component.text(text).color(convertColor(ColorText.WHITE)));
        return this;
    }

    @Override
    public MessageBuilder append(String text, ColorText colorText) {
        components.add(Component.text(text).color(convertColor(colorText)));
        return this;
    }

    @Override
    public void build() {
        component = Component.text().append(components).build();
    }

    @Override
    public void BukkitSender() {
        if (component != null) {
            Bukkit.getConsoleSender().sendMessage(component);
        } else {
            Bukkit.getConsoleSender().sendMessage("Component is not built yet.");
        }
    }

    private NamedTextColor convertColor(ColorText colorText) {
        switch (colorText) {
            case BLACK: return NamedTextColor.BLACK;
            case DARK_BLUE: return NamedTextColor.DARK_BLUE;
            case DARK_GREEN: return NamedTextColor.DARK_GREEN;
            case DARK_AQUA: return NamedTextColor.DARK_AQUA;
            case DARK_RED: return NamedTextColor.DARK_RED;
            case DARK_PURPLE: return NamedTextColor.DARK_PURPLE;
            case GOLD: return NamedTextColor.GOLD;
            case GRAY: return NamedTextColor.GRAY;
            case DARK_GRAY: return NamedTextColor.DARK_GRAY;
            case BLUE: return NamedTextColor.BLUE;
            case GREEN: return NamedTextColor.GREEN;
            case AQUA: return NamedTextColor.AQUA;
            case RED: return NamedTextColor.RED;
            case LIGHT_PURPLE: return NamedTextColor.LIGHT_PURPLE;
            case YELLOW: return NamedTextColor.YELLOW;
            case WHITE: return NamedTextColor.WHITE;
            default: return NamedTextColor.WHITE;
        }
    }
}