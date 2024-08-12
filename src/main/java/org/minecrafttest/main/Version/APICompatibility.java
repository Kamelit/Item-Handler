package org.minecrafttest.main.Version;

public final class APICompatibility {

    private static final boolean COMPONENT;
    private static final boolean METADATA;
    private static final boolean FOLIA;

    static {
        COMPONENT = checkModernAPIComponent();
        METADATA = checkModernAPIMeta();
        FOLIA = isFolia();
    }

    private static boolean checkModernAPIComponent() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean checkModernAPIMeta() {
        try {
            Class.forName("org.bukkit.NamespacedKey");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isFolia(){
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isModernAPIComponent() {
        return COMPONENT;
    }

    public static boolean isModernAPIMeta(){
        return METADATA;
    }

    public static boolean isFoliaApi(){
        return FOLIA;
    }
}