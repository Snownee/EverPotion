package snownee.everpotion;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig
public final class EverCommonConfig {

	@Range(min = 5)
	public static int drinkDelay = 20;
	@Range(min = 5)
	public static int refillTime = 2400;
	@Range(min = 5)
	public static int tipArrowTimeCost = 300;
	public static boolean naturallyRefill = true;
	@Range(min = 0, max = 10)
	@Comment("Damaging mobs can speed up refilling")
	public static double damageAcceleration = 1;

	@Path("slots.maxSlots")
	@Range(min = 1, max = 4)
	public static int maxSlots = 3;
	@Path("slots.beginnerSlots")
	@Range(min = 0, max = 4)
	public static int beginnerSlots = 0;

	@Path("effects.durationFactor")
	@Range(min = 0, max = 100)
	public static double durationFactor = 1;
	@Path("effects.ambient")
	public static boolean ambient = true;
	@Path("effects.showParticles")
	public static boolean showParticles = true;
	@Path("effects.showIcon")
	public static boolean showIcon = true;

	@Path("temp.mobDropUnlockItem")
	@Range(min = 0, max = 1)
	public static double mobDropUnlockItem = 0.005;

}
