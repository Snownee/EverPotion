package snownee.everpotion.plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import snownee.everpotion.EverPotion;
import snownee.everpotion.crafting.CraftingModule;
import snownee.everpotion.crafting.EverAnvilRecipe;
import snownee.kiwi.Kiwi;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final ResourceLocation UID = new ResourceLocation(EverPotion.MODID, "anvil");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!Kiwi.isLoaded(new ResourceLocation(EverPotion.MODID, "crafting"))) {
            return;
        }
        World world = Minecraft.getInstance().world;
        if (world == null) {
            return;
        }

        List<AnvilRecipe> recipes = world.getRecipeManager().getRecipes(CraftingModule.RECIPE_TYPE).values().stream().map($ -> {
            EverAnvilRecipe recipe = (EverAnvilRecipe) $;
            return new AnvilRecipe(ImmutableList.copyOf(recipe.getLeft().getMatchingStacks()), ImmutableList.copyOf(recipe.getRight().getMatchingStacks()), Collections.singletonList(recipe.getRecipeOutput()));
        }).collect(Collectors.toList());

        registration.addRecipes(recipes, VanillaRecipeCategoryUid.ANVIL);
    }

}
