package snownee.everpotion.datagen;

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.everpotion.PotionType;
import snownee.everpotion.crafting.CraftingModule;

public class EverAnvilRecipeProvider extends RecipeProvider {

	private static final Ingredient RIGHT = Ingredient.fromTag(CraftingModule.INGREDIENT);

	public EverAnvilRecipeProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	@Override
	public String getName() {
		return "EverPotion Recipes";
	}

	@Override
	protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
		register(Potions.WATER, null, PotionType.SPLASH, 2, consumer);
		Map<Effect, Pair<EffectInstance, Potion>> effects = Maps.newHashMap();
		for (Potion potion : ForgeRegistries.POTION_TYPES) {
			if (potion.getEffects().size() != 1)
				continue;
			EffectInstance effect = potion.getEffects().get(0);
			if (!accept(effect))
				continue;
			if (!"minecraft".equals(effect.getPotion().getRegistryName().getNamespace()))
				continue;
			Pair<EffectInstance, Potion> exist = effects.get(effect.getPotion());
			if (exist != null && exist.getLeft().getDuration() > effect.getDuration())
				continue;
			effects.put(effect.getPotion(), Pair.of(effect, potion));
		}
		effects.put(Effects.RESISTANCE, Pair.of(new EffectInstance(Effects.RESISTANCE, 6000), Potions.TURTLE_MASTER));
		for (Pair<EffectInstance, Potion> pair : effects.values()) {
			for (PotionType type : PotionType.values()) {
				register(pair.getRight(), pair.getLeft(), type, getCharge(pair.getLeft().getPotion()), consumer);
			}
		}
	}

	private static boolean accept(EffectInstance effect) {
		int amplifier = effect.getAmplifier();
		Effect potion = effect.getPotion();
		if (potion == Effects.JUMP_BOOST || potion == Effects.SPEED) {
			return amplifier == 1;
		}
		return amplifier == 0;
	}

	private static float getCharge(Effect effect) {
		if (effect == Effects.INSTANT_HEALTH)
			return .9f;
		if (effect == Effects.NIGHT_VISION || effect == Effects.INVISIBILITY)
			return .8f;
		return 1;
	}

	private void register(Potion potion, @Nullable EffectInstance effect, PotionType type, float charge, Consumer<IFinishedRecipe> consumer) {
		Ingredient left = new AccessIt(PotionUtils.addPotionToItemStack(new ItemStack(type.potionItem), potion));
		EverAnvilRecipeBuilder.coreRecipe(effect, type, charge).left(left).right(RIGHT).levelCost(type.level).build(consumer);
	}

	private static class AccessIt extends NBTIngredient {
		private AccessIt(ItemStack stack) {
			super(stack);
		}

		@Override
		public JsonElement serialize() {
			JsonObject json = (JsonObject) super.serialize();
			if (json.get("count").getAsInt() == 1)
				json.remove("count");
			return json;
		}
	}
}
