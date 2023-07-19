package snownee.everpotion.util;

import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.everpotion.CoreModule;
import snownee.everpotion.client.EverPotionClient;
import snownee.everpotion.client.PotionCoreSkillClientHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.skill.PotionCoreSkill;
import snownee.skillslots.client.SkillSlotsClient;

public class ClientProxy {
	public static void init() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(ClientProxy::registerItemColors);
		eventBus.addListener(ClientProxy::registerRenderers);
	}

	private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
		EverPotionClient.registerItemColors(event::register);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(CoreModule.ARROW.get(), TippableArrowRenderer::new);
	}

	public static void loadComplete() {
		ItemProperties.register(CoreModule.CORE.get(), new ResourceLocation("type"), (stack, world, entity, seed) -> {
			return CoreItem.getPotionType(stack).ordinal() * 0.1F;
		});
		SkillSlotsClient.registerClientHandler(PotionCoreSkill.class, new PotionCoreSkillClientHandler());
	}
}
