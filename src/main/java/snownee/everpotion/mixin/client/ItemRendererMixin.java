package snownee.everpotion.mixin.client;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import snownee.skillslots.SkillSlotsHandler;
import snownee.everpotion.skill.PotionCoreSkill;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Shadow
	public float blitOffset;

	@Inject(
			at = @At(
					"TAIL"
			), method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
	)
	public void everpotion$renderGuiItemDecorations(Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, CallbackInfo info) {
		if (!(stack.getItem() instanceof ProjectileWeaponItem))
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.screen != null)
			return;
		ItemStack mainhand = player.getMainHandItem();
		ItemStack offhand = player.getOffhandItem();
		if (mainhand != stack && offhand != stack)
			return;
		SkillSlotsHandler handler = SkillSlotsHandler.of(player);
		PotionCoreSkill skill = PotionCoreSkill.findTipIndex(handler);
		if (skill == null)
			return;
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();

		MobEffectTextureManager textures = mc.getMobEffectTextures();
		TextureAtlasSprite sprite = textures.get(Objects.requireNonNull(skill.effect).getEffect());
		RenderSystem.setShaderTexture(0, sprite.atlas().location());

		float width = 9;
		float left = xPosition + 9;
		float top = yPosition + 4;

		Gui.blit(new PoseStack(), (int) left, (int) top, (int) blitOffset, (int) width, (int) width, sprite);

		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
	}
}