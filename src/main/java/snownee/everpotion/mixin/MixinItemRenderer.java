package snownee.everpotion.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShootableItem;
import net.minecraftforge.common.util.LazyOptional;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.everpotion.handler.EverHandler.Cache;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(at = @At("TAIL"), method = "renderItemOverlayIntoGUI")
    public void everpotion_renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, CallbackInfo info) {
        if (!(stack.getItem() instanceof ShootableItem))
            return;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        ItemStack mainhand = player.getHeldItemMainhand();
        ItemStack offhand = player.getHeldItemOffhand();
        if (mainhand != stack && offhand != stack)
            return;
        LazyOptional<EverHandler> optional = player.getCapability(EverCapabilities.HANDLER);
        if (!optional.isPresent())
            return;
        EverHandler handler = optional.orElse(null);
        if (!handler.canUseSlot(handler.tipIndex, false))
            return;
        Cache cache = handler.caches[handler.tipIndex];
        if (cache == null || cache.effect == null)
            return;
        RenderSystem.disableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        PotionSpriteUploader potionspriteuploader = Minecraft.getInstance().getPotionSpriteUploader();
        TextureAtlasSprite sprite = potionspriteuploader.getSprite(cache.effect.getPotion());
        sprite.getAtlasTexture().bindTexture();

        float width = 9;
        float left = xPosition + 9;
        float right = left + width;
        float top = yPosition + 4;
        float bottom = top + width;

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(left, bottom, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
        buffer.pos(right, bottom, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
        buffer.pos(right, top, 0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
        buffer.pos(left, top, 0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
        tessellator.draw();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }
}
