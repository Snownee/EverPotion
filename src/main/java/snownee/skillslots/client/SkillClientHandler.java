package snownee.skillslots.client;

import java.util.function.IntUnaryOperator;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import snownee.skillslots.skill.Skill;

public interface SkillClientHandler<T extends Skill> {

	void renderGUI(T skill, PoseStack matrix, float xCenter, float yCenter, float scale, float alpha, int textColor, MutableInt textYOffset);

	void pickColor(T skill, IntUnaryOperator saturationModifier);
}
