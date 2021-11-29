package snownee.everpotion.crafting;

import java.util.Optional;

import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
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
	public static final EverAnvilRecipe.Serializer SERIALIZER = new EverAnvilRecipe.Serializer();

	public static final Tag.Named<Item> INGREDIENT = itemTag(EverPotion.MODID, "ingredient");

	@SubscribeEvent
	public void onAnvilCrafting(AnvilUpdateEvent event) {
		AnvilContext ctx = new AnvilContext(event);
		RecipeManager manager = Util.getRecipeManager();
		if (manager == null)
			return;
		Optional<EverAnvilRecipe> result = manager.getRecipeFor(RECIPE_TYPE, ctx, null);
		result.ifPresent(recipe -> {
			event.setOutput(recipe.assemble(ctx));
			event.setCost(ctx.cost);
			event.setMaterialCost(ctx.materialCost);
		});
	}

}
