package snownee.skillslots;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;
import snownee.skillslots.skill.Skill;

public class SkillSlots {
	public static final String ID = "skillslots";

	public static final List<Function<ItemStack, Skill>> SKILL_FACTORIES = Lists.newArrayList();

	public static void registerSkillFactory(Function<ItemStack, Skill> factory) {
		SKILL_FACTORIES.add(0, factory);
	}

	public static Skill createSkill(ItemStack stack) {
		if (stack.isEmpty()) {
			return Skill.EMPTY;
		}
		for (Function<ItemStack, Skill> factory : SKILL_FACTORIES) {
			Skill skill = factory.apply(stack);
			if (skill != null) {
				return skill;
			}
		}
		return Skill.EMPTY;
	}
}
