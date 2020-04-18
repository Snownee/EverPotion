package snownee.everpotion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.cap.EverCapabilityProvider;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.client.gui.PlaceScreen;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.container.PlaceContainer;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.CDrinkPacket;
import snownee.everpotion.network.COpenContainerPacket;
import snownee.everpotion.network.SSyncPotionsPacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.network.NetworkChannel;

@KiwiModule
@KiwiModule.Subscriber
@KiwiModule.Group("brewing")
public class CoreModule extends AbstractModule {

    public static final CoreItem CORE = new CoreItem();

    public static final UnlockSlotItem UNLOCK_SLOT = new UnlockSlotItem();

    public static final ContainerType<PlaceContainer> MAIN = new ContainerType<>(PlaceContainer::new);

    public CoreModule() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EverCommonConfig.spec);
        modEventBus.register(EverCommonConfig.class);
        // if (FMLEnvironment.dist.isClient()) {
        //     ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModNameClientConfig.spec);
        //     modEventBus.register(ModNameClientConfig.class);
        // }
    }

    @Override
    protected void preInit() {
        NetworkChannel.register(CDrinkPacket.class, new CDrinkPacket.Handler());
        NetworkChannel.register(COpenContainerPacket.class, new COpenContainerPacket.Handler());
        NetworkChannel.register(SSyncPotionsPacket.class, new SSyncPotionsPacket.Handler());
    }

    @Override
    protected void init(FMLCommonSetupEvent event) {
        EverCommonConfig.refresh();

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
        MinecraftForge.EVENT_BUS.register(ClientHandler.class);
    }

    public static final ResourceLocation HANDLER_ID = new ResourceLocation(EverPotion.MODID, "handler");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(HANDLER_ID, new EverCapabilityProvider(new EverHandler((PlayerEntity) event.getObject())));
        }
    }

    @SubscribeEvent
    protected void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        //        Scheduler.add(new SimpleGlobalTask(LogicalSide.SERVER, Phase.END, t -> {
        //            if (t >= 10) {
        //                sync((ServerPlayerEntity) event.getPlayer());
        //                return true;
        //            }
        //            return false;
        //        }));
    }

    public static void sync(ServerPlayerEntity player) {
        new SSyncPotionsPacket(player).send();
    }

    @SubscribeEvent
    public void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        event.player.getCapability(EverCapabilities.HANDLER).ifPresent(EverHandler::tick);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurt(LivingHurtEvent event) {
        event.getEntity().getCapability(EverCapabilities.HANDLER).ifPresent(EverHandler::stopDrinking);
        if (event.getEntity().world.isRemote) {
            if (Minecraft.getInstance().currentScreen instanceof UseScreen) {
                Minecraft.getInstance().displayGuiScreen(null);
            }
        }
        // TODO
    }
}
