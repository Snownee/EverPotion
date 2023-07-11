package snownee.everpotion.client;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.math.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.ItemLike;
import snownee.everpotion.CoreModule;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.COpenContainerPacket;
import snownee.kiwi.util.MathUtil;

public final class EverPotionClient {

	public static final KeyMapping kbUse = new KeyMapping("keybind.everpotion.use", GLFW.GLFW_KEY_R, "gui.everpotion.keygroup");

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
		consumer.accept((stack, i) -> {
			if (i == 1) {
				int tier = UnlockSlotItem.getTier(stack);
				return switch (tier) {
					default -> 16733525;
					case 1 -> 16777215;
					case 2 -> 16777045;
					case 3 -> 5636095;
					case 4 -> 16733695;
				};
			}
			return -1;
		}, CoreModule.UNLOCK_SLOT.get());
	}

	public static void onKeyInput() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null || mc.player.isSpectator()) {
			return;
		}
		while (kbUse.consumeClick()) {
			EverHandler handler = EverHandler.of(mc.player);
			if (mc.player.isShiftKeyDown()) {
				if (handler.getSlots() == 0) {
					mc.player.displayClientMessage(Component.translatable("msg.everpotion.noSlots"), true);
					return;
				}
				COpenContainerPacket.I.sendToServer($ -> {
				});
			} else {
				mc.setScreen(new UseScreen());
			}
		}
	}

	public static void playSound(SoundEvent soundEvent) {
		playSound(soundEvent, 1);
	}

	public static void playSound(SoundEvent soundEvent, float vol) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, vol));
	}

}
