package snownee.everpotion;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class EverCommonConfig {

    public static final ForgeConfigSpec spec;

    public static int drinkDelay = 20;
    public static int refillTime = 2400;
    public static float damageAcceleration = 1;

    // slots
    public static int maxSlots = 3;
    public static int beginnerSlots = 0;

    // effects
    public static float durationFactor = 1;
    public static boolean ambient = true;
    public static boolean showParticles = true;
    public static boolean showIcon = true;

    public static float mobDropUnlockItem = 0.005f;

    private static IntValue drinkDelayVal;
    private static IntValue refillTimeVal;
    private static DoubleValue damageAccelerationVal;

    private static IntValue maxSlotsVal;
    private static IntValue beginnerSlotsVal;

    private static DoubleValue durationFactorVal;
    private static BooleanValue ambientVal;
    private static BooleanValue showParticlesVal;
    private static BooleanValue showIconVal;

    private static DoubleValue mobDropUnlockItemVal;

    static {
        spec = new ForgeConfigSpec.Builder().configure(EverCommonConfig::new).getRight();
    }

    private EverCommonConfig(ForgeConfigSpec.Builder builder) {
        drinkDelayVal = builder.defineInRange("drinkDelay", drinkDelay, 5, 100000);
        refillTimeVal = builder.defineInRange("refillTime", refillTime, 5, 100000);
        damageAccelerationVal = builder.comment("Damaging mobs can speed up refilling").defineInRange("damageAcceleration", damageAcceleration, 0, 10);

        builder.push("slots");
        maxSlotsVal = builder.defineInRange("maxSlots", maxSlots, 1, 4);
        beginnerSlotsVal = builder.defineInRange("beginnerSlots", beginnerSlots, 0, 4);

        builder.pop().push("effects");
        durationFactorVal = builder.defineInRange("durationFactor", durationFactor, 0, 100);
        ambientVal = builder.define("ambient", ambient);
        showParticlesVal = builder.define("showParticles", showParticles);
        showIconVal = builder.define("showIcon", showIcon);

        builder.pop().push("temp");
        mobDropUnlockItemVal = builder.defineInRange("mobDropUnlockItem", mobDropUnlockItem, 0, 1);
    }

    public static void refresh() {
        drinkDelay = drinkDelayVal.get();
        refillTime = refillTimeVal.get();
        damageAcceleration = damageAccelerationVal.get().floatValue();
        maxSlots = maxSlotsVal.get();
        beginnerSlots = beginnerSlotsVal.get();
        durationFactor = durationFactorVal.get().floatValue();
        ambient = ambientVal.get();
        showParticles = showParticlesVal.get();
        showIcon = showIconVal.get();
        mobDropUnlockItem = mobDropUnlockItemVal.get().floatValue();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
