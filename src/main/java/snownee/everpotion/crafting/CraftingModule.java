package snownee.everpotion.crafting;

import java.util.List;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.everpotion.EverPotion;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.util.Util;

@KiwiModule("crafting")
@KiwiModule.Optional
@KiwiModule.Subscriber
public class CraftingModule extends AbstractModule {

	@Name("anvil")
	public static final RecipeType<EverAnvilRecipe> RECIPE_TYPE = new RecipeType<>() {
	};

	@Name("anvil")
	public static final EverAnvilRecipe.Serializer<EverAnvilRecipe> SERIALIZER = new EverAnvilRecipe.Serializer<>(EverAnvilRecipe::new);

	public static final Tag.Named<Item> INGREDIENT = itemTag(EverPotion.MODID, "ingredient");

	@SubscribeEvent
	public void onAnvilCrafting(AnvilUpdateEvent event) {
		AnvilContext ctx = new AnvilContext(event);
		RecipeManager manager = Util.getRecipeManager();
		if (manager == null)
			return;
		Level level = null;
		if (event.getPlayer() != null)
			level = event.getPlayer().level;

		List<EverAnvilRecipe> results = manager.getRecipesFor(RECIPE_TYPE, ctx, level);
		if (!results.isEmpty()) {
			EverAnvilRecipe recipe = results.get(0);
			for (EverAnvilRecipe result : results) {
				if (!result.isSpecial()) {
					recipe = result;
					break;
				}
			}
			event.setOutput(recipe.assemble(ctx));
			event.setCost(ctx.cost);
			event.setMaterialCost(ctx.materialCost);
		}
	}

}
