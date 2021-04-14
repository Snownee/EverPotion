package snownee.everpotion;

import java.util.Locale;

public enum PotionType {
    NORMAL, SPLASH, LINGERING;

    private String descKey;

    public String getDescKey() {
        if (descKey == null) {
            descKey = "tip.everpotion.potionType." + toString();
        }
        return descKey;
    }

    public static PotionType parse(String s) {
        switch (s) {
        case "splash":
            return SPLASH;
        case "lingering":
            return LINGERING;
        default:
            return NORMAL;
        }
    }

    public static PotionType valueOf(byte b) {
        switch (b) {
        case 1:
            return SPLASH;
        case 2:
            return LINGERING;
        default:
            return NORMAL;
        }
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }
}
