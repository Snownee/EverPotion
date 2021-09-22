package snownee.everpotion.data;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.everpotion.PotionType;
import snownee.everpotion.crafting.CraftingModule;

public class EverAnvilRecipeProvider extends RecipeProvider {

	private static final Ingredient RIGHT = Ingredient.of(CraftingModule.INGREDIENT);

	public EverAnvilRecipeProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	@Override
	public String getName() {
		return "EverPotion Recipes";
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
		register(Potions.WATER, null, PotionType.SPLASH, 2, consumer);
		Map<MobEffect, Pair<MobEffectInstance, Potion>> effects = Maps.newHashMap();
		for (Potion potion : ForgeRegistries.POTIONS) {
			if (potion.getEffects().size() != 1)
				continue;
			MobEffectInstance effect = potion.getEffects().get(0);
			if (!accept(effect))
				continue;
			if (!effect.getEffect().getRegistryName().getNamespace().equals("minecraft"))
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

	private void register(Potion potion, @Nullable MobEffectInstance effect, PotionType type, float charge, Consumer<FinishedRecipe> consumer) {
		Ingredient left = new AccessIt(PotionUtils.setPotion(new ItemStack(type.potionItem), potion));
		EverAnvilRecipeBuilder.coreRecipe(effect, type, charge).left(left).right(RIGHT).levelCost(type.level).build(consumer);
	}

	private static class AccessIt extends NBTIngredient {
		private AccessIt(ItemStack stack) {
			super(stack);
		}

		@Override
		public JsonElement toJson() {
			JsonObject json = (JsonObject) super.toJson();
			if (json.get("count").getAsInt() == 1)
				json.remove("count");
			return json;
		}
	}
}
