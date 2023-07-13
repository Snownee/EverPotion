package snownee.everpotion.datagen;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import snownee.everpotion.CoreModule;
import snownee.everpotion.PotionType;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.lychee.Lychee;
import snownee.skillslots.SkillSlots;
import snownee.skillslots.SkillSlotsModule;

public class EverAnvilRecipeProvider extends RecipeProvider {

	private static final Ingredient RIGHT = Ingredient.of(CoreModule.INGREDIENT);

	public EverAnvilRecipeProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	private static boolean accept(MobEffectInstance effect) {
		int amplifier = effect.getAmplifier();
		MobEffect potion = effect.getEffect();
		if (potion == MobEffects.JUMP || potion == MobEffects.MOVEMENT_SPEED) {
			return amplifier == 1;
		}
		return amplifier == 0;
	}

	private static float getCharge(MobEffect effect) {
		if (effect == MobEffects.HEAL)
			return .9f;
		if (effect == MobEffects.NIGHT_VISION || effect == MobEffects.INVISIBILITY)
			return .8f;
		return 1;
	}

	@Override
	public String getName() {
		return "EverPotion Recipes";
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
		register(Potions.WATER, null, PotionType.SPLASH, 2, consumer);
		Map<MobEffect, Pair<MobEffectInstance, Potion>> effects = Maps.newHashMap();
		for (Potion potion : Registry.POTION) {
			if (potion.getEffects().size() != 1)
				continue;
			MobEffectInstance effect = potion.getEffects().get(0);
			if (!accept(effect))
				continue;
			if (!Registry.MOB_EFFECT.getKey(effect.getEffect()).getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
				continue;
			Pair<MobEffectInstance, Potion> exist = effects.get(effect.getEffect());
			if (exist != null && exist.getLeft().getDuration() > effect.getDuration())
				continue;
			effects.put(effect.getEffect(), Pair.of(effect, potion));
		}
		effects.put(MobEffects.DAMAGE_RESISTANCE, Pair.of(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000), Potions.TURTLE_MASTER));
		for (Pair<MobEffectInstance, Potion> pair : effects.values()) {
			for (PotionType type : PotionType.values()) {
				register(pair.getRight(), pair.getLeft(), type, getCharge(pair.getLeft().getEffect()), consumer);
			}
		}

		SingleItemRecipeBuilder stonecutting = SingleItemRecipeBuilder.stonecutting(Ingredient.of(Items.ECHO_SHARD), SkillSlotsModule.UNLOCK_SLOT.get());
		stonecutting.unlockedBy(getHasName(Items.ECHO_SHARD), has(Items.ECHO_SHARD));
		ConditionalRecipe.builder()
				.addCondition(new ModuleLoadedCondition(new ResourceLocation(SkillSlots.ID, "slot_unlock_recipe")))
				.addRecipe(stonecutting::save)
				.build(consumer, new ResourceLocation(SkillSlots.ID, "slot_unlock"));
	}

	private void register(Potion potion, @Nullable MobEffectInstance effect, PotionType type, float charge, Consumer<FinishedRecipe> consumer) {
		ItemStack potionItem = PotionUtils.setPotion(new ItemStack(type.potionItem), potion);
		Ingredient left = PartialNBTIngredient.of(type.potionItem, potionItem.getOrCreateTag());
		EverAnvilRecipeBuilder.coreRecipe(effect, type, charge).left(left).right(RIGHT).levelCost(type.level).whenModLoaded(Lychee.ID).build(consumer);
	}

}
