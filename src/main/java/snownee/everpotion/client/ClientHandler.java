package snownee.everpotion.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.settings.KeyModifier;
import snownee.everpotion.CoreModule;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.item.UnlockSlotItem;
import snownee.everpotion.network.COpenContainerPacket;
import snownee.kiwi.util.MathUtil;

@OnlyIn(Dist.CLIENT)
public final class ClientHandler {

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
                Vector3f hsv = MathUtil.RGBtoHSV(rgb);
                hsv.mul(1, .75f, 1);
                return MathHelper.hsvToRGB(hsv.getX(), hsv.getY(), hsv.getZ());
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

    public static final KeyBinding kbUse = new KeyBinding("keybind.everpotion.use", GLFW.GLFW_KEY_R, "gui.everpotion.keygroup");

    public static void onKeyInput(KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.currentScreen != null || mc.player.isSpectator()) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && kbUse.isKeyDown()) {
            EverHandler handler = mc.player.getCapability(EverCapabilities.HANDLER).orElse(null);
            if (handler == null) {
                return;
            }
            if (mc.player.isCrouching()) {
                if (handler.getSlots() == 0) {
                    mc.ingameGUI./*addChatMessage*/func_238450_a_(ChatType.GAME_INFO, new TranslationTextComponent("msg.everpotion.noSlots"), Util.DUMMY_UUID);
                    return;
                }
                new COpenContainerPacket().send();
            } else if (kbUse.getKeyModifier().isActive(null)) {
                if (kbUse.getKeyModifier() == KeyModifier.NONE && event.getModifiers() != 0) {
                    return;
                }
                mc.displayGuiScreen(new UseScreen());
            }
        }
    }

    public static void renderOverlay(RenderGameOverlayEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getType() == ElementType.CROSSHAIRS && mc.currentScreen != null && mc.currentScreen.getClass() == UseScreen.class) {
            event.setCanceled(true);
        }
    }

}
