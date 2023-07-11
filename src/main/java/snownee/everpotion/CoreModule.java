package snownee.everpotion;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.cap.EverCapabilityProvider;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.client.gui.PlaceScreen;
import snownee.everpotion.datagen.EverAnvilRecipeProvider;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.menu.PlaceMenu;
import snownee.everpotion.network.SCancelPacket;
import snownee.everpotion.network.SSyncPotionsPacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.schedule.impl.SimpleGlobalTask;

@KiwiModule
@KiwiModule.Subscriber
@KiwiModule.Category("brewing")
public class CoreModule extends AbstractModule {

	public static final ResourceLocation HANDLER_ID = new ResourceLocation(EverPotion.ID, "handler");
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
	public static final KiwiGO<MenuType<PlaceMenu>> MAIN = go(() -> new MenuType<>(PlaceMenu::new));

	public CoreModule() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		if (Platform.isPhysicalClient()) {
			eventBus.addListener(ClientHandler::onItemColorsInit);
			eventBus.addListener(ClientHandler::registerRenderers);
		}
		eventBus.addListener(this::registerCap);
	}

	public static void sync(ServerPlayer player, boolean filled) {
		if (player instanceof FakePlayer) {
			return;
		}
		SSyncPotionsPacket.send(player, filled);
	}

	protected void registerCap(RegisterCapabilitiesEvent event) {
		event.register(EverHandler.class);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		MenuScreens.register(MAIN.get(), PlaceScreen::new);
		MinecraftForge.EVENT_BUS.addListener(ClientHandler::renderOverlay);
		MinecraftForge.EVENT_BUS.addListener(ClientHandler::onKeyInput);
		MinecraftForge.EVENT_BUS.addListener(ClientHandler::registerKeyMapping);
		ItemProperties.register(CORE.get(), new ResourceLocation("type"), (stack, world, entity, seed) -> {
			return CoreItem.getPotionType(stack).ordinal();
		});
	}

	@SubscribeEvent
	protected void onCommandsRegister(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> builder = EverCommand.init(event.getDispatcher());
		event.getDispatcher().register(builder);
	}

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)) {
			event.addCapability(HANDLER_ID, new EverCapabilityProvider(new EverHandler((Player) event.getObject())));
		}
	}

	@SubscribeEvent
	protected void onPlayerJoinWorld(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity.level.isClientSide) {
			return;
		}
		if (entity instanceof ServerPlayer && !(entity instanceof FakePlayer)) {
			Scheduler.add(new SimpleGlobalTask(LogicalSide.SERVER, Phase.END, t -> {
				if (t >= 5) {
					sync((ServerPlayer) entity, false);
					return true;
				}
				return false;
			}));
		}
	}

	@SubscribeEvent
	public void tickPlayer(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}
		event.player.getCapability(EverCapabilities.HANDLER).ifPresent(EverHandler::tick);
	}

	@SubscribeEvent
	public void onLivingDamage(LivingDamageEvent event) {
		if (event.getEntity().level.isClientSide) {
			return;
		}
		if (!(event.getSource() instanceof EntityDamageSource)) {
			return;
		}
		LivingEntity living = event.getEntity();
		living.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
			handler.stopDrinking();
			if (living instanceof ServerPlayer) {
				SCancelPacket.I.send((ServerPlayer) living, $ -> {
				});
			}
		});

		Entity source = event.getSource().getEntity();
		if (source instanceof ServerPlayer && EverCommonConfig.damageAcceleration > 0) {
			source.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
				handler.accelerate(.05f * event.getAmount() * (float) EverCommonConfig.damageAcceleration);
				if (source.level.random.nextBoolean()) {
					sync((ServerPlayer) source, false);
				}
			});
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		EverHandler newHandler = event.getEntity().getCapability(EverCapabilities.HANDLER).orElse(null);
		Player original = event.getOriginal();
		original.reviveCaps();
		EverHandler oldHandler = original.getCapability(EverCapabilities.HANDLER).orElse(null);
		original.invalidateCaps();
		if (newHandler != null && oldHandler != null) {
			newHandler.copyFrom(oldHandler);
		}
	}

	@Override
	public void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		generator.addProvider(event.includeServer(), new EverAnvilRecipeProvider(generator));
	}

}
