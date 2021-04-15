package snownee.everpotion;

import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum PotionType {
	NORMAL(8, Items.POTION), SPLASH(10, Items.SPLASH_POTION), LINGERING(12, Items.LINGERING_POTION);

	private String descKey;
	public final int level;
	public final Item potionItem;

	private PotionType(int level, Item potionItem) {
		this.level = level;
		this.potionItem = potionItem;
	}

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
