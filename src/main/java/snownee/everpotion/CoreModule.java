package snownee.everpotion;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.cap.EverCapabilityProvider;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.client.gui.PlaceScreen;
import snownee.everpotion.container.PlaceContainer;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.CDrinkPacket;
import snownee.everpotion.network.COpenContainerPacket;
import snownee.everpotion.network.SCancelPacket;
import snownee.everpotion.network.SSyncPotionsPacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.network.NetworkChannel;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.schedule.impl.SimpleGlobalTask;

@KiwiModule
@KiwiModule.Subscriber
@KiwiModule.Group("brewing")
public class CoreModule extends AbstractModule {

    public static final CoreItem CORE = new CoreItem();

    public static final UnlockSlotItem UNLOCK_SLOT = new UnlockSlotItem();

    public static final ContainerType<PlaceContainer> MAIN = new ContainerType<>(PlaceContainer::new);

    public CoreModule() {
        if (FMLEnvironment.dist.isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::onItemColorsInit);
        }
    }

    @Override
    protected void preInit() {
        NetworkChannel.register(CDrinkPacket.class, new CDrinkPacket.Handler());
        NetworkChannel.register(COpenContainerPacket.class, new COpenContainerPacket.Handler());
        NetworkChannel.register(SSyncPotionsPacket.class, new SSyncPotionsPacket.Handler());
        NetworkChannel.register(SCancelPacket.class, new SCancelPacket.Handler());
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(EverHandler.class, new Capability.IStorage<EverHandler>() {

            @Override
            public INBT writeNBT(Capability<EverHandler> capability, EverHandler instance, Direction side) {
                return new CompoundNBT();
            }

            @Override
            public void readNBT(Capability<EverHandler> capability, EverHandler instance, Direction side, INBT nbt) {}

        }, EverHandler::new);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientInit(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(ClientHandler.kbUse);
        ScreenManager.registerFactory(MAIN, PlaceScreen::new);
        MinecraftForge.EVENT_BUS.addListener(ClientHandler::renderOverlay);
        MinecraftForge.EVENT_BUS.addListener(ClientHandler::onKeyInput);
        ItemModelsProperties.func_239418_a_(CORE, new ResourceLocation("type"), (stack, world, entity) -> {
            return CoreItem.getPotionType(stack).ordinal();
        });
    }

    @SubscribeEvent
    protected void onCommandsRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSource> builder = EverCommand.init(event.getDispatcher());
        event.getDispatcher().register(builder);
    }

    public static final ResourceLocation HANDLER_ID = new ResourceLocation(EverPotion.MODID, "handler");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity && !(event.getObject() instanceof FakePlayer)) {
            event.addCapability(HANDLER_ID, new EverCapabilityProvider(new EverHandler((PlayerEntity) event.getObject())));
        }
    }

    @SubscribeEvent
    protected void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity.world.isRemote) {
            return;
        }
        if (entity instanceof ServerPlayerEntity && !(entity instanceof FakePlayer)) {
            Scheduler.add(new SimpleGlobalTask(LogicalSide.SERVER, Phase.END, t -> {
                if (t >= 5) {
                    sync((ServerPlayerEntity) entity);
                    return true;
                }
                return false;
            }));
        }
    }

    public static void sync(ServerPlayerEntity player) {
        if (player instanceof FakePlayer) {
            return;
        }
        new SSyncPotionsPacket(player).send();
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
        if (event.getEntity().world.isRemote) {
            return;
        }
        LivingEntity living = event.getEntityLiving();
        living.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
            handler.stopDrinking();
            if (living instanceof ServerPlayerEntity) {
                new SCancelPacket().send((ServerPlayerEntity) living);

            }
        });

        Entity source = event.getSource().getTrueSource();
        if (source instanceof ServerPlayerEntity && EverCommonConfig.damageAcceleration > 0) {
            source.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
                handler.accelerate(.05f * event.getAmount() * (float) EverCommonConfig.damageAcceleration);
                if (source.world.rand.nextBoolean()) {
                    sync((ServerPlayerEntity) source);
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
        if (EverCommonConfig.mobDropUnlockItem == 0 || event.getEntity().world.isRemote) {
            return;
        }
        Entity source = event.getSource().getTrueSource();
        if (source instanceof PlayerEntity && event.getEntity() instanceof MobEntity) {
            if (event.getEntityLiving().getRNG().nextFloat() < EverCommonConfig.mobDropUnlockItem) {
                event.getEntityLiving().entityDropItem(UNLOCK_SLOT);
            }
        }
    }

}
