package snownee.everpotion.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.everpotion.CoreModule;
import snownee.everpotion.client.EverPotionClient;
import snownee.everpotion.client.gui.PlaceScreen;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.item.CoreItem;

public class ClientProxy {
	public static void init() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(ClientProxy::registerItemColors);
		eventBus.addListener(ClientProxy::registerRenderers);

		MinecraftForge.EVENT_BUS.addListener(ClientProxy::renderOverlay);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::onKeyInput);
		MinecraftForge.EVENT_BUS.addListener(ClientProxy::registerKeyMapping);
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		EverPotionClient.registerItemColors(event::register);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(CoreModule.ARROW.get(), TippableArrowRenderer::new);
	}

	private static void onKeyInput(InputEvent.Key event) {
		if (event.getAction() == GLFW.GLFW_PRESS) {
			EverPotionClient.onKeyInput();
		}
	}

	private static void renderOverlay(RenderGuiOverlayEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type() && mc.screen != null && mc.screen.getClass() == UseScreen.class) {
			event.setCanceled(true);
		}
	}

	private static void registerKeyMapping(RegisterKeyMappingsEvent event) {
		event.register(EverPotionClient.kbUse);
	}

	public static void loadComplete() {
		MenuScreens.register(CoreModule.PLACE.get(), PlaceScreen::new);
		ItemProperties.register(CoreModule.CORE.get(), new ResourceLocation("type"), (stack, world, entity, seed) -> {
			return CoreItem.getPotionType(stack).ordinal();
		});
	}
}
