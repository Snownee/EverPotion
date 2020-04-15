package snownee.everpotion.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.everpotion.CoreModule;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.COpenContainerPacket;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public final class ClientHandler {

    @SubscribeEvent
    public static void onItemColorsInit(ColorHandlerEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.register((stack, i) -> {
            if (i == 0) {
                EffectInstance effect = CoreItem.getEffectInstance(stack);
                int rgb;
                if (effect != null) {
                    rgb = effect.getPotion().getLiquidColor();
                } else {
                    rgb = 3694022;
                }
                Vector3f hsv = RGBtoHSV(rgb);
                hsv.mul(1, .75f, 1);
                return MathHelper.hsvToRGB(hsv.getX() / 360, hsv.getY(), hsv.getZ() / 255);
            }
            return -1;
        }, CoreModule.CORE);
        colors.register((stack, i) ->

        {
            if (i == 1) {
                int tier = UnlockSlotItem.getTier(stack);
                switch (tier) {
                default:
                    return 16733525;
                case 1:
                    return 16777215;
                case 2:
                    return 16777045;
                case 3:
                    return 5636095;
                case 4:
                    return 16733695;
                }
            }
            return -1;
        }, CoreModule.UNLOCK_SLOT);
    }

    public static final KeyBinding kbUse = new KeyBinding("keybind.everpotion.use", GLFW.GLFW_KEY_Z, "gui.everpotion.keygroup");

    @SubscribeEvent
    public static void onKeyInput(KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.currentScreen != null) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && kbUse.isKeyDown()) {
            if (mc.player.isCrouching()) {
                new COpenContainerPacket().send();
            } else if (event.getModifiers() == 0) {
                mc.displayGuiScreen(new UseScreen());
            }
        }
    }

    /**
     * @return h: 0-360 s: 0-1 v: 0-255
     */
    public static Vector3f RGBtoHSV(int rgb) {
        int r = (rgb >> 16) & 255;
        int g = (rgb >> 8) & 255;
        int b = rgb & 255;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        float v = max;
        float delta = max - min;
        float h, s;
        if (max != 0)
            s = delta / max; // s
        else {
            // r = g = b = 0        // s = 0, v is undefined
            s = 0;
            h = -1;
            return new Vector3f(h, s, 0 /*Float.NaN*/);
        }
        if (r == max)
            h = (g - b) / delta; // between yellow & magenta
        else if (g == max)
            h = 2 + (b - r) / delta; // between cyan & yellow
        else
            h = 4 + (r - g) / delta; // between magenta & cyan
        h *= 60; // degrees
        if (h < 0)
            h += 360;
        return new Vector3f(h, s, v);
    }

}
