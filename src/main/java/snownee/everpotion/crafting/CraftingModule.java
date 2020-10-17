package snownee.everpotion.crafting;

import java.util.Optional;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;

@KiwiModule("crafting")
@KiwiModule.Optional
@KiwiModule.Subscriber
public class CraftingModule extends AbstractModule {

    @Name("anvil")
    public static final IRecipeType<EverAnvilRecipe> RECIPE_TYPE = new IRecipeType<EverAnvilRecipe>() {};

    @Name("anvil")
    public static final IRecipeSerializer<EverAnvilRecipe> SERIALIZER = new EverAnvilRecipe.Serializer();

    @SubscribeEvent
    public void onAnvilCrafting(AnvilUpdateEvent event) {
        AnvilContext ctx = new AnvilContext(event);
        Optional<EverAnvilRecipe> result = Kiwi.getServer().getRecipeManager().getRecipe(RECIPE_TYPE, ctx, null);
        result.ifPresent(recipe -> {
            event.setOutput(recipe.getCraftingResult(ctx));
            event.setCost(ctx.cost);
            event.setMaterialCost(ctx.materialCost);
        });
    }

}
