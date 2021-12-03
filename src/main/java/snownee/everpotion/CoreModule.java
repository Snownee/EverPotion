package snownee.everpotion;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.cap.EverCapabilityProvider;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.client.gui.PlaceScreen;
import snownee.everpotion.data.EverAnvilRecipeProvider;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.menu.PlaceMenu;
import snownee.everpotion.network.SCancelPacket;
import snownee.everpotion.network.SSyncPotionsPacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.schedule.impl.SimpleGlobalTask;

@KiwiModule
@KiwiModule.Subscriber
@KiwiModule.Category("brewing")
public class CoreModule extends AbstractModule {

	public static final CoreItem CORE = new CoreItem();

	public static final UnlockSlotItem UNLOCK_SLOT = new UnlockSlotItem();

	public static final EntityType<EverArrow> ARROW = EntityType.Builder.<EverArrow>of(EverArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("everpotion:arrow");

	public static final MenuType<PlaceMenu> MAIN = new MenuType<>(PlaceMenu::new);

	public CoreModule() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		if (Platform.isPhysicalClient()) {
			eventBus.addListener(ClientHandler::onItemColorsInit);
			eventBus.addListener(ClientHandler::registerRenderers);
		}
		eventBus.addListener(this::registerCap);
	}

	protected void registerCap(RegisterCapabilitiesEvent event) {
		event.register(EverHandler.class);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		ClientRegistry.registerKeyBinding(ClientHandler.kbUse);
		MenuScreens.register(MAIN, PlaceScreen::new);
		MinecraftForge.EVENT_BUS.addListener(ClientHandler::renderOverlay);
		MinecraftForge.EVENT_BUS.addListener(ClientHandler::onKeyInput);
		ItemProperties.register(CORE, new ResourceLocation("type"), (stack, world, entity, seed) -> {
			return CoreItem.getPotionType(stack).ordinal();
		});
	}

	@SubscribeEvent
	protected void onCommandsRegister(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> builder = EverCommand.init(event.getDispatcher());
		event.getDispatcher().register(builder);
	}

	public static final ResourceLocation HANDLER_ID = new ResourceLocation(EverPotion.MODID, "handler");

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)) {
			event.addCapability(HANDLER_ID, new EverCapabilityProvider(new EverHandler((Player) event.getObject())));
		}
	}

	@SubscribeEvent
	protected void onPlayerJoinWorld(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity.level.isClientSide) {
			return;
		}
		if (entity instanceof ServerPlayer && !(entity instanceof FakePlayer)) {
			Scheduler.add(new SimpleGlobalTask(LogicalSide.SERVER, Phase.END, t -> {
				if (t >= 5) {
					sync((ServerPlayer) entity);
					return true;
				}
				return false;
			}));
		}
	}

	public static void sync(ServerPlayer player) {
		if (player instanceof FakePlayer) {
			return;
		}
		SSyncPotionsPacket.send(player);
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
		LivingEntity living = event.getEntityLiving();
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
					sync((ServerPlayer) source);
				}
			});
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		EverHandler newHandler = event.getPlayer().getCapability(EverCapabilities.HANDLER).orElse(null);
		EverHandler oldHandler = event.getOriginal().getCapability(EverCapabilities.HANDLER).orElse(null);
		if (newHandler != null && oldHandler != null) {
			newHandler.copyFrom(oldHandler);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void tempLoot(LivingDeathEvent event) {
		if (EverCommonConfig.mobDropUnlockItem == 0 || event.getEntity().level.isClientSide) {
			return;
		}
		Entity source = event.getSource().getEntity();
		if (source instanceof Player && event.getEntity() instanceof Mob) {
			if (event.getEntityLiving().getRandom().nextFloat() < EverCommonConfig.mobDropUnlockItem) {
				event.getEntityLiving().spawnAtLocation(UNLOCK_SLOT);
			}
		}
	}

	@Override
	public void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		generator.addProvider(new EverAnvilRecipeProvider(generator));
	}

}
