package snownee.everpotion.compat;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverPotion;
import snownee.everpotion.crafting.CraftingModule;
import snownee.everpotion.crafting.EverAnvilRecipe;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.Util;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(EverPotion.MODID, "anvil");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(CoreModule.CORE);
		registration.useNbtForSubtypes(CoreModule.UNLOCK_SLOT);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (!Kiwi.isLoaded(new ResourceLocation(EverPotion.MODID, "crafting"))) {
			return;
		}
		Level level = Minecraft.getInstance().level;
		if (level == null) {
			return;
		}

		List<IJeiAnvilRecipe> recipes = Util.getRecipes(CraftingModule.RECIPE_TYPE).stream().filter($ -> {
			return !$.getResultItem().isEmpty();
		}).map($ -> {
			EverAnvilRecipe recipe = (EverAnvilRecipe) $;
			return registration.getVanillaRecipeFactory().createAnvilRecipe(ImmutableList.copyOf(recipe.getLeft().getItems()), ImmutableList.copyOf(recipe.getRight().getItems()), Collections.singletonList(recipe.getResultItem()));
		}).collect(Collectors.toList());

		registration.addRecipes(RecipeTypes.ANVIL, recipes);
	}

}
