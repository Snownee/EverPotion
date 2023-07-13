package snownee.skillslots;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;

@KiwiConfig(value = "skillslots-client", type = ConfigType.CLIENT)
public final class SkillSlotsClientConfig {

	public static boolean chargeCompleteNotificationSound = true;

}
