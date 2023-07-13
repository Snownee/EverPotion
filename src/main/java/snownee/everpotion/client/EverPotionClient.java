package snownee.everpotion.client;

import java.util.function.BiConsumer;

import com.mojang.math.Vector3f;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ItemLike;
import snownee.everpotion.CoreModule;
import snownee.everpotion.item.CoreItem;
import snownee.kiwi.util.MathUtil;

public final class EverPotionClient {

	public static void registerItemColors(BiConsumer<ItemColor, ItemLike> consumer) {
		consumer.accept((stack, i) -> {
			if (i == 0) {
				MobEffectInstance effect = CoreItem.getEffectInstance(stack);
				int rgb;
				if (effect != null) {
					rgb = effect.getEffect().getColor();
				} else {
					rgb = 3694022;
				}
				Vector3f hsv = MathUtil.RGBtoHSV(rgb);
				hsv.mul(1, .75f, 1);
				return Mth.hsvToRgb(hsv.x(), hsv.y(), hsv.z());
			}
			return -1;
		}, CoreModule.CORE.get());
	}

}
