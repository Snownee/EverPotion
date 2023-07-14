package snownee.skillslots.client;

import java.util.function.IntUnaryOperator;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import snownee.skillslots.skill.Skill;
import snownee.skillslots.util.ClientProxy;

public class SimpleSkillClientHandler implements SkillClientHandler<Skill> {
	@Override
	public void renderGUI(Skill skill, PoseStack matrix, float xCenter, float yCenter, float scale, float alpha, int textColor, MutableInt textYOffset) {
		Font font = Minecraft.getInstance().font;
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

		if (alpha > 0.3F) {
			float yCenter2 = yCenter - 6 * (1 + 0.125f * scale);
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.translate(xCenter, yCenter2, 0);
			scale = 1.5F + scale * 0.25F;
			CompoundTag tag = skill.item.getTagElement("SkillSlots");
			if (tag != null && tag.contains("IconScale")) {
				scale *= tag.getFloat("IconScale");
			}
			modelViewStack.scale(scale, scale, scale);
			modelViewStack.translate(-8, -8, 0);
			itemRenderer.renderAndDecorateItem(skill.item, 0, 0);
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();
		}

		if (skill.item.getCount() != 1) {
			matrix.pushPose();
			matrix.translate(xCenter, yCenter + 10, 300);
			matrix.scale(0.75f, 0.75f, 0.75f);
			GuiComponent.drawString(matrix, font, Integer.toString(skill.item.getCount()), 6, -12, textColor);
			matrix.popPose();
		}
	}

	@Override
	public void pickColor(Skill skill, IntUnaryOperator saturationModifier) {
		skill.color = saturationModifier.applyAsInt(ClientProxy.pickItemColor(skill.item, skill.color));
	}
}
