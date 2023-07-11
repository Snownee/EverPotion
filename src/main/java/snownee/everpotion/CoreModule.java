package snownee.everpotion;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.menu.PlaceMenu;
import snownee.everpotion.network.SCancelPacket;
import snownee.everpotion.network.SSyncPotionsPacket;
import snownee.everpotion.util.ClientProxy;
import snownee.everpotion.util.CommonProxy;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.event.ClientInitEvent;

@KiwiModule
@KiwiModule.Category("brewing")
public class CoreModule extends AbstractModule {

	public static final TagKey<Item> INGREDIENT = itemTag(EverPotion.ID, "ingredient");
	public static final KiwiGO<CoreItem> CORE = go(CoreItem::new);
	public static final KiwiGO<UnlockSlotItem> UNLOCK_SLOT = go(UnlockSlotItem::new);
	public static final KiwiGO<EntityType<EverArrow>> ARROW = go(() -> EntityType.Builder.<EverArrow>of(EverArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("everpotion:arrow"));
	public static final KiwiGO<SoundEvent> FILL_COMPLETE_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "fill_complete")));
	public static final KiwiGO<SoundEvent> USE_NORMAL_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "use_normal")));
	public static final KiwiGO<SoundEvent> USE_SPLASH_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "use_splash")));
	public static final KiwiGO<SoundEvent> CHARGE_SHORT_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "charge_short")));
	public static final KiwiGO<SoundEvent> CHARGE_LONG_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "charge_long")));
	public static final KiwiGO<SoundEvent> HOVER_SOUND = go(() -> new SoundEvent(new ResourceLocation(EverPotion.ID, "hover")));
	public static final KiwiGO<MenuType<PlaceMenu>> PLACE = go(() -> new MenuType<>(PlaceMenu::new));

	public static void sync(ServerPlayer player, boolean filled) {
		if (CommonProxy.isFakePlayer(player)) {
			return;
		}
		SSyncPotionsPacket.send(player, filled);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(EverCommand.init(dispatcher));
	}

	public static void playerLoggedIn(ServerPlayer player) {
		sync(player, false);
	}

	public static void causeDamage(DamageSource source, LivingEntity target, float amount) {
		if (target.level.isClientSide) {
			return;
		}
		if (!(source instanceof EntityDamageSource)) {
			return;
		}

		if (EverCommonConfig.interruptedOnHurt && target instanceof ServerPlayer) {
			EverHandler targetHandler = EverHandler.of(target);
			if (targetHandler != null) {
				targetHandler.stopDrinking();
				SCancelPacket.I.send((ServerPlayer) target, $ -> {
				});
			}
		}

		if (EverCommonConfig.damageAcceleration > 0 && source.getEntity() instanceof ServerPlayer sourceEntity) {
			EverHandler.of(sourceEntity).accelerate(.05f * amount * (float) EverCommonConfig.damageAcceleration);
			if (sourceEntity.level.random.nextBoolean()) {
				sync(sourceEntity, false);
			}
		}
	}

	public static void clonePlayer(Player original, Player clone) {
		EverHandler newHandler = EverHandler.of(clone);
		EverHandler oldHandler = EverHandler.of(original);
		newHandler.copyFrom(oldHandler);
	}

	@Override
	protected void clientInit(ClientInitEvent event) {
		event.enqueueWork(ClientProxy::loadComplete);
	}
}
