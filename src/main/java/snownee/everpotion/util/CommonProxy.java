package snownee.everpotion.util;

import net.minecraft.data.DataGenerator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import snownee.everpotion.EverPotion;
import snownee.everpotion.datagen.EverAnvilRecipeProvider;
import snownee.kiwi.loader.Platform;

@Mod(EverPotion.ID)
public class CommonProxy {

	public CommonProxy() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(this::gatherData);
		if (Platform.isPhysicalClient()) {
			ClientProxy.init();
		}
	}

	public static Packet<?> getEntitySpawningPacket(Entity entity) {
		return NetworkHooks.getEntitySpawningPacket(entity);
	}

	private void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		generator.addProvider(event.includeServer(), new EverAnvilRecipeProvider(generator));
	}

}
