package snownee.everpotion.crafting;

import java.util.Optional;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import snownee.kiwi.AbstractModule;
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

    public static MinecraftServer SERVER;

    @Override
    protected void serverInit(FMLServerStartingEvent event) {
        SERVER = event.getServer();
    }

    public void serverStopped(FMLServerStoppedEvent event) {
        SERVER = null;
    }

    public void onAnvilCrafting(AnvilUpdateEvent event) {
        if (SERVER == null) {
            return;
        }
        AnvilContext ctx = new AnvilContext(event);
        Optional<EverAnvilRecipe> result = SERVER.getRecipeManager().getRecipe(RECIPE_TYPE, ctx, null);
        result.ifPresent(recipe -> {
            event.setOutput(recipe.getCraftingResult(ctx));
            event.setCost(ctx.cost);
            event.setMaterialCost(ctx.materialCost);
        });
    }

}
