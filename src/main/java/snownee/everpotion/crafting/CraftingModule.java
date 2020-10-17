package snownee.everpotion.crafting;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;

@KiwiModule(name = "crafting")
@KiwiModule.Optional
@KiwiModule.Subscriber
public class CraftingModule extends AbstractModule {

    @Name("anvil")
    public static final IRecipeType<EverAnvilRecipe> RECIPE_TYPE = new IRecipeType<EverAnvilRecipe>() {};

    @Name("anvil")
    public static final IRecipeSerializer<EverAnvilRecipe> SERIALIZER = new EverAnvilRecipe.Serializer();

    @SubscribeEvent
    public void onAnvilCrafting(AnvilUpdateEvent event) {
        RecipeManager manager = getRecipeManager();
        if (manager == null) {
            return;
        }
        AnvilContext ctx = new AnvilContext(event);
        Optional<EverAnvilRecipe> result = manager.getRecipe(RECIPE_TYPE, ctx, null);
        result.ifPresent(recipe -> {
            event.setOutput(recipe.getCraftingResult(ctx));
            event.setCost(ctx.cost);
            event.setMaterialCost(ctx.materialCost);
        });
    }

    @Nullable
    public static RecipeManager getRecipeManager() {
        MinecraftServer server = Kiwi.getServer();
        if (server != null) {
            return server.getRecipeManager();
        } else if (FMLEnvironment.dist.isClient()) {
            ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                return connection.getRecipeManager();
            }
        }
        return null;
    }

}
