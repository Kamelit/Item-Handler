package org.minecrafttest.main.Version;

import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.Component.LegacyMessageBuilder;
import org.minecrafttest.main.Version.Component.ModernMessageBuilder;

public interface MessageBuilder {

    MessageBuilder append(String text);
    MessageBuilder append(String text, ColorText colorText);
    void build();
    void BukkitSender();

    static MessageBuilder createMessageBuilder() {
        if (APICompatibility.isModernAPI()) {
            return new ModernMessageBuilder();
        } else {
            return new LegacyMessageBuilder();
        }
    }
}