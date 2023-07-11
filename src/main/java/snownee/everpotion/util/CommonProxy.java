package snownee.everpotion.util;

import net.minecraft.data.DataGenerator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverPotion;
import snownee.everpotion.datagen.EverAnvilRecipeProvider;
import snownee.kiwi.loader.Platform;

@Mod(EverPotion.ID)
public class CommonProxy {

	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::playerLoggedIn);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::causeDamage);
		MinecraftForge.EVENT_BUS.addListener(this::clonePlayer);
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::gatherData);
		if (Platform.isPhysicalClient()) {
			ClientProxy.init();
		}
	}

	public static boolean isFakePlayer(Player player) {
		return player instanceof FakePlayer;
	}

	public static Packet<?> getEntitySpawningPacket(Entity entity) {
		return NetworkHooks.getEntitySpawningPacket(entity);
	}


	private void registerCommands(RegisterCommandsEvent event) {
		CoreModule.registerCommands(event.getDispatcher());
	}

	private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			CoreModule.playerLoggedIn(player);
		}
	}

	private void causeDamage(LivingDamageEvent event) {
		CoreModule.causeDamage(event.getSource(), event.getEntity(), event.getAmount());
	}

	private void clonePlayer(PlayerEvent.Clone event) {
		CoreModule.clonePlayer(event.getOriginal(), event.getEntity());
	}

	private void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		generator.addProvider(event.includeServer(), new EverAnvilRecipeProvider(generator));
	}

}
