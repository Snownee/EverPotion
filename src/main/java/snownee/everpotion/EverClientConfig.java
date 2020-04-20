package snownee.everpotion;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class EverClientConfig {

    public static final ForgeConfigSpec spec;

    static {
        spec = new ForgeConfigSpec.Builder().configure(EverClientConfig::new).getRight();
    }

    private EverClientConfig(ForgeConfigSpec.Builder builder) {
        builder.push("hud");
    }

    public static void refresh() {}

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
