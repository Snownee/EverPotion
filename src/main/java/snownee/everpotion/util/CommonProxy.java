package snownee.everpotion.util;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import snownee.everpotion.EverPotion;
import snownee.everpotion.datagen.EverAnvilRecipeProvider;
import snownee.kiwi.Mod;

@Mod(EverPotion.ID)
public class CommonProxy implements DataGeneratorEntrypoint {

	public static Packet<?> getEntitySpawningPacket(Entity entity) {
		return new ClientboundAddEntityPacket(entity);
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		generator.addProvider(new EverAnvilRecipeProvider(generator));
	}
}
