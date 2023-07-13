package snownee.skillslots;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.LevelRestart;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig("skillslots-common")
public final class SkillSlotsCommonConfig {

	public static boolean naturallyCharging = true;
	@Range(min = 0, max = 10)
	@Comment("Damaging mobs can speed up refilling")
	public static double damageAcceleration = 1;
	public static boolean interruptedOnHurt;

	@Path("slots.maxSlots")
	@Range(min = 1, max = 4)
	@LevelRestart
	public static int maxSlots = 3;
	@Path("slots.beginnerSlots")
	@Range(min = 0, max = 4)
	@LevelRestart
	public static int beginnerSlots = 0;
}
