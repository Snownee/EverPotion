package snownee.everpotion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.util.ClientProxy;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.event.ClientInitEvent;

@KiwiModule
public class CoreModule extends AbstractModule {

	public static final TagKey<Item> INGREDIENT = itemTag(EverPotion.ID, "ingredient");
	@KiwiModule.Category("brewing")
	public static final KiwiGO<CoreItem> CORE = go(CoreItem::new);
	public static final KiwiGO<EntityType<EverArrow>> ARROW = go(() -> EntityType.Builder.<EverArrow>of(EverArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("everpotion:arrow"));
	public static final KiwiGO<SoundEvent> USE_NORMAL_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "use_normal")));
	public static final KiwiGO<SoundEvent> USE_SPLASH_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "use_splash")));

	@Override
	protected void clientInit(ClientInitEvent event) {
		event.enqueueWork(ClientProxy::loadComplete);
	}
}
