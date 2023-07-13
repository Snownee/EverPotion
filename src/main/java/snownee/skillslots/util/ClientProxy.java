package snownee.skillslots.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.loader.Platform;
import snownee.skillslots.SkillSlotsModule;
import snownee.skillslots.client.SimpleSkillClientHandler;
import snownee.skillslots.client.SkillSlotsClient;
import snownee.skillslots.client.gui.PlaceScreen;
import snownee.skillslots.client.gui.UseScreen;
import snownee.skillslots.compat.JEIPlugin;
import snownee.skillslots.skill.Skill;

public class ClientProxy {
	private static final boolean hasJEI = Platform.isModLoaded("jei");

	public static void init() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(ClientProxy::registerItemColors);

		MinecraftForge.EVENT_BUS.addListener(ClientProxy::renderOverlay);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::onKeyInput);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::registerKeyMapping);
	}

	public static int pickItemColor(ItemStack stack, int fallback) {
		int color = Minecraft.getInstance().getItemColors().getColor(stack, 0);
		if (color != -1) {
			return color;
		}
		if (hasJEI) {
			return JEIPlugin.pickItemColor(stack, fallback);
		}
		return fallback;
	}

	public static void loadComplete() {
		MenuScreens.register(SkillSlotsModule.PLACE.get(), PlaceScreen::new);
		SkillSlotsClient.registerClientHandler(Skill.class, new SimpleSkillClientHandler());
	}

	private static void onKeyInput(InputEvent.Key event) {
		if (event.getAction() == GLFW.GLFW_PRESS) {
			SkillSlotsClient.onKeyInput();
		}
	}

	private static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type() && mc.screen != null && mc.screen.getClass() == UseScreen.class) {
			event.setCanceled(true);
		}
	}

	private static void registerKeyMapping(RegisterKeyMappingsEvent event) {
		event.register(SkillSlotsClient.kbUse);
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		SkillSlotsClient.registerItemColors(event::register);
	}
}
