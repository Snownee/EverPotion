package snownee.everpotion;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.LevelRestart;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig
public final class EverCommonConfig {

	@Range(min = 5)
	public static int drinkDelay = 20;
	@Range(min = 5)
	public static int refillTime = 2400;
	@Range(min = 5)
	public static int tipArrowTimeCost = 600;
	public static boolean naturallyRefill = true;
	@Range(min = 0, max = 10)
	@Comment("Damaging mobs can speed up refilling")
	public static double damageAcceleration = 1;

	@Path("slots.maxSlots")
	@Range(min = 1, max = 4)
	@LevelRestart
	public static int maxSlots = 3;
	@Path("slots.beginnerSlots")
	@Range(min = 0, max = 4)
	@LevelRestart
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

}
