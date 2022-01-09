package snownee.everpotion.crafting;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule(value = "fallback_recipe", dependencies = "@crafting")
@KiwiModule.Optional
public class FallbackRecipeModule extends AbstractModule {

	public static final EverAnvilRecipe.Serializer<FallbackCoreRecipe> FALLBACK = new EverAnvilRecipe.Serializer<>(FallbackCoreRecipe::new);

}
