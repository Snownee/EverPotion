package snownee.everpotion.crafting;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.lychee.core.recipe.LycheeRecipe;

@KiwiModule(value = "fallback_recipe", dependencies = "lychee")
@KiwiModule.Optional
public class FallbackRecipeModule extends AbstractModule {

	public static final KiwiGO<LycheeRecipe.Serializer<FallbackCoreRecipe>> FALLBACK = go(FallbackCoreRecipe.Serializer::new);

}
