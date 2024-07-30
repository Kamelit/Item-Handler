package org.minecrafttest.main.Version;

public final class APICompatibility {

    private static final boolean MODERN_API;

    static {
        MODERN_API = checkModernAPI();
    }

    private static boolean checkModernAPI() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isModernAPI() {
        return MODERN_API;
    }
}