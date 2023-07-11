package snownee.everpotion;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kiwi.loader.Platform;

@EventBusSubscriber
public final class EverPotion {

	public static final String ID = "everpotion";

	public static final boolean hasLychee = Platform.isModLoaded("lychee");

}
