package snownee.everpotion.client;

import java.util.function.IntUnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import snownee.everpotion.skill.PotionCoreSkill;
import snownee.skillslots.client.SkillClientHandler;

public class PotionCoreSkillClientHandler implements SkillClientHandler<PotionCoreSkill> {
	@Override
	public void renderGUI(PotionCoreSkill skill, PoseStack matrix, float xCenter, float yCenter, float scale, float alpha, Component name, int textColor) {
		Font font = Minecraft.getInstance().font;
		float yCenter2 = yCenter - 6 * (1 + 0.125f * scale);
		if (skill.effect != null) {
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			BufferBuilder buffer = Tesselator.getInstance().getBuilder();
			Matrix4f matrix4f = matrix.last().pose();
			MobEffectTextureManager potionspriteuploader = Minecraft.getInstance().getMobEffectTextures();
			TextureAtlasSprite sprite = potionspriteuploader.get(skill.effect.getEffect());
			RenderSystem.setShaderTexture(0, sprite.atlas().location());

			float halfwidth = 12 + 1.5f * scale;
			float left = xCenter - halfwidth;
			float right = xCenter + halfwidth;
			float top = yCenter2 - halfwidth;
			float bottom = yCenter2 + halfwidth;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			buffer.vertex(matrix4f, left, bottom, 0).uv(sprite.getU0(), sprite.getV1()).endVertex();
			buffer.vertex(matrix4f, right, bottom, 0).uv(sprite.getU1(), sprite.getV1()).endVertex();
			buffer.vertex(matrix4f, right, top, 0).uv(sprite.getU1(), sprite.getV0()).endVertex();
			buffer.vertex(matrix4f, left, top, 0).uv(sprite.getU0(), sprite.getV0()).endVertex();
			BufferUploader.drawWithShader(buffer.end());

			matrix.translate(xCenter, yCenter + 10, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			GuiComponent.drawCenteredString(matrix, font, name, 0, 0, textColor);
		} else {
			matrix.translate(xCenter, yCenter - 3, 0);
			matrix.scale(0.75f, 0.75f, 0.75f);
			GuiComponent.drawCenteredString(matrix, font, name, 0, 0, textColor);
		}
	}

	@Override
	public void pickColor(PotionCoreSkill skill, IntUnaryOperator saturationModifier) {
		if (skill.effect != null) {
			skill.color = saturationModifier.applyAsInt(skill.effect.getEffect().getColor());
		} else {
			skill.color = 4749311; // 3694022;
		}
	}
}
