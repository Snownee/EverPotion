package snownee.everpotion;

import java.util.Locale;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum PotionType {
	NORMAL(8, Items.POTION, 0.2F), SPLASH(10, Items.SPLASH_POTION, 0.2F), LINGERING(12, Items.LINGERING_POTION, 0.1F);

	private String descKey;
	public final int level;
	public final Item potionItem;
	public final float durationFactor;

	private PotionType(int level, Item potionItem, float durationFactor) {
		this.level = level;
		this.potionItem = potionItem;
		this.durationFactor = durationFactor;
	}

	public String getDescKey() {
		if (descKey == null) {
			descKey = "tip.everpotion.potionType." + this;
		}
		return descKey;
	}

	public static PotionType parse(String s) {
		return switch (s) {
			case "splash" -> SPLASH;
			case "lingering" -> LINGERING;
			default -> NORMAL;
		};
	}

	public static PotionType valueOf(byte b) {
		return switch (b) {
			case 1 -> SPLASH;
			case 2 -> LINGERING;
			default -> NORMAL;
		};
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase(Locale.ENGLISH);
	}

}
