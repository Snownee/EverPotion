package snownee.skillslots.client;

import java.util.function.IntUnaryOperator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import snownee.skillslots.skill.Skill;
import snownee.skillslots.util.ClientProxy;

public class SimpleSkillClientHandler implements SkillClientHandler<Skill> {
	@Override
	public void renderGUI(Skill skill, PoseStack matrix, float xCenter, float yCenter, float scale, float alpha, Component name, int textColor) {
		Font font = Minecraft.getInstance().font;
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		if (alpha > 0.3F) {
			float yCenter2 = yCenter - 6 * (1 + 0.125f * scale);
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.translate(xCenter, yCenter2, 0);
			scale = 1.5F + scale * 0.25F;
			modelViewStack.scale(scale, scale, scale);
			modelViewStack.translate(-8, -8, 0);
			itemRenderer.renderAndDecorateItem(skill.item, 0, 0);
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();
		}

		matrix.translate(xCenter, yCenter + 10, 300);
		matrix.scale(0.75f, 0.75f, 0.75f);
		GuiComponent.drawCenteredString(matrix, font, name, 0, 0, textColor);
		if (skill.item.getCount() != 1)
			GuiComponent.drawString(matrix, font, Integer.toString(skill.item.getCount()), 6, -12, textColor);
	}

	@Override
	public void pickColor(Skill skill, IntUnaryOperator saturationModifier) {
		skill.color = saturationModifier.applyAsInt(ClientProxy.pickItemColor(skill.item, skill.color));
	}
}
