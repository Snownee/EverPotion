package snownee.everpotion.util;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import snownee.everpotion.CoreModule;
import snownee.everpotion.client.EverPotionClient;
import snownee.everpotion.client.PotionCoreSkillClientHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.skill.PotionCoreSkill;
import snownee.skillslots.client.SkillSlotsClient;

public class ClientProxy {

	public static void loadComplete() {
		ItemProperties.register(CoreModule.CORE.get(), new ResourceLocation("type"), (stack, world, entity, seed) -> {
			return CoreItem.getPotionType(stack).ordinal() * 0.1F;
		});
		SkillSlotsClient.registerClientHandler(PotionCoreSkill.class, new PotionCoreSkillClientHandler());
		EntityRendererRegistry.register(CoreModule.ARROW.get(), TippableArrowRenderer::new);
		EverPotionClient.registerItemColors(ColorProviderRegistry.ITEM::register);
	}

}
