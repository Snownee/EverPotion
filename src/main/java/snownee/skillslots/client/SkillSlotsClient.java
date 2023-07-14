package snownee.skillslots.client;

import java.util.Map;
import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.ItemLike;
import snownee.skillslots.SkillSlotsCommonConfig;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.SkillSlotsModule;
import snownee.skillslots.client.gui.UseScreen;
import snownee.skillslots.item.UnlockSlotItem;
import snownee.skillslots.network.COpenContainerPacket;
import snownee.skillslots.skill.Skill;

public final class SkillSlotsClient {

	public static final KeyMapping kbUse = new KeyMapping("keybind.skillslots.use", GLFW.GLFW_KEY_R, "gui.skillslots.keygroup");

	private static final Map<Class<? extends Skill>, SkillClientHandler<?>> CLIENT_HANDLERS = Maps.newIdentityHashMap();

	public static void registerItemColors(BiConsumer<ItemColor, ItemLike> consumer) {
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
		}, SkillSlotsModule.UNLOCK_SLOT.get());
	}

	public static void onKeyInput() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null || mc.player.isSpectator()) {
			return;
		}
		while (kbUse.consumeClick()) {
			SkillSlotsHandler handler = SkillSlotsHandler.of(mc.player);
			if (SkillSlotsCommonConfig.playerCustomizable && mc.player.isShiftKeyDown()) {
				if (handler.getContainerSize() == 0) {
					mc.player.displayClientMessage(Component.translatable("msg.skillslots.noSlots"), true);
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

	public static <T extends Skill> SkillClientHandler<T> getClientHandler(T skill) {
		return (SkillClientHandler<T>) CLIENT_HANDLERS.get(skill.getClass());
	}

	public static <T extends Skill> void registerClientHandler(Class<T> skill, SkillClientHandler<T> handler) {
		CLIENT_HANDLERS.put(skill, handler);
	}

}
