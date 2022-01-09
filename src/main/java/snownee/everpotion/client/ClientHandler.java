package snownee.everpotion.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.math.Vector3f;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.settings.KeyModifier;
import snownee.everpotion.CoreModule;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.COpenContainerPacket;
import snownee.kiwi.util.MathUtil;

@OnlyIn(Dist.CLIENT)
public final class ClientHandler {

	public static void onItemColorsInit(ColorHandlerEvent.Item event) {
		ItemColors colors = event.getItemColors();
		colors.register((stack, i) -> {
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
		}, CoreModule.CORE);
		colors.register((stack, i) -> {
			if (i == 1) {
				int tier = UnlockSlotItem.getTier(stack);
				switch (tier) {
				default:
					return 16733525;
				case 1:
					return 16777215;
				case 2:
					return 16777045;
				case 3:
					return 5636095;
				case 4:
					return 16733695;
				}
			}
			return -1;
		}, CoreModule.UNLOCK_SLOT);
	}

	public static void registerRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(CoreModule.ARROW, TippableArrowRenderer::new);
	}

	public static final KeyMapping kbUse = new KeyMapping("keybind.everpotion.use", GLFW.GLFW_KEY_R, "gui.everpotion.keygroup");

	public static void onKeyInput(KeyInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null || mc.player.isSpectator()) {
			return;
		}
		if (event.getAction() == GLFW.GLFW_PRESS && kbUse.isDown()) {
			EverHandler handler = mc.player.getCapability(EverCapabilities.HANDLER).orElse(null);
			if (handler == null) {
				return;
			}
			if (mc.player.isCrouching()) {
				if (handler.getSlots() == 0) {
					mc.player.displayClientMessage(new TranslatableComponent("msg.everpotion.noSlots"), true);
					return;
				}
				COpenContainerPacket.I.sendToServer($ -> {
				});
			} else if (kbUse.getKeyModifier().isActive(null)) {
				if (kbUse.getKeyModifier() == KeyModifier.NONE && event.getModifiers() != 0) {
					return;
				}
				mc.setScreen(new UseScreen());
			}
		}
	}

	public static void renderOverlay(RenderGameOverlayEvent.PreLayer event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getOverlay() == ForgeIngameGui.CROSSHAIR_ELEMENT && mc.screen != null && mc.screen.getClass() == UseScreen.class) {
			event.setCanceled(true);
		}
	}

	public static void playSound(SoundEvent soundEvent) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1));
	}

}
