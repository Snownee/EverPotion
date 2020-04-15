package snownee.everpotion.crafting;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.everpotion.CoreModule;
import snownee.everpotion.PotionType;
import snownee.kiwi.crafting.Recipe;

public class EverAnvilRecipe extends Recipe<AnvilContext> {

    private final Ingredient left;
    private final Ingredient right;
    private final int cost;
    private final int materialCost;
    public final ItemStack output;

    public EverAnvilRecipe(ResourceLocation id, Ingredient left, Ingredient right, int cost, int materialCost, ItemStack output) {
        super(id);
        this.left = left;
        this.right = right;
        this.cost = cost;
        this.materialCost = materialCost;
        this.output = output;
    }

    @Override
    public boolean matches(AnvilContext inv, World worldIn) {
        return left.test(inv.left) && right.test(inv.right);
    }

    @Override
    public ItemStack getCraftingResult(AnvilContext inv) {
        inv.cost = cost;
        inv.materialCost = materialCost;
        return getRecipeOutput();
    }

    @Override
    public ItemStack getRecipeOutput() {
        return output.copy();
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return CraftingModule.SERIALIZER;
    }

    @Override
    public IRecipeType<?> getType() {
        return CraftingModule.RECIPE_TYPE;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EverAnvilRecipe> {

        @Override
        public EverAnvilRecipe read(ResourceLocation recipeId, JsonObject json) {
            Ingredient left = CraftingHelper.getIngredient(json.get("left"));
            Ingredient right = CraftingHelper.getIngredient(json.get("right"));
            int cost = JSONUtils.getInt(json, "cost", 0);
            int materialCost = JSONUtils.getInt(json, "materialCost", 1);
            JsonObject outputObj = JSONUtils.getJsonObject(json, "output");
            ItemStack output;
            if (outputObj.has("effect")) {
                Effect effect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(JSONUtils.getString(outputObj, "effect")));
                int duration = JSONUtils.getInt(outputObj, "duration", 0) * 20;
                int amplifier = JSONUtils.getInt(outputObj, "amplifier", 0);
                EffectInstance effectInstance = null;
                if (effect != null) {
                    effectInstance = new EffectInstance(effect, duration, amplifier);
                }
                PotionType type = PotionType.parse(JSONUtils.getString(outputObj, "type", "normal"));
                float charge = JSONUtils.getFloat(outputObj, "charge", 1);
                output = CoreModule.CORE.make(effectInstance, type, charge);
            } else {
                output = CraftingHelper.getItemStack(outputObj, true);
            }
            return new EverAnvilRecipe(recipeId, left, right, cost, materialCost, output);
        }

        @Override
        public EverAnvilRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient left = Ingredient.read(buffer);
            Ingredient right = Ingredient.read(buffer);
            int cost = buffer.readVarInt();
            int materialCost = buffer.readVarInt();
            ItemStack output = buffer.readItemStack();
            return new EverAnvilRecipe(recipeId, left, right, cost, materialCost, output);
        }

        @Override
        public void write(PacketBuffer buffer, EverAnvilRecipe recipe) {
            recipe.left.write(buffer);
            recipe.right.write(buffer);
            buffer.writeVarInt(recipe.cost);
            buffer.writeVarInt(recipe.materialCost);
            buffer.writeItemStack(recipe.output);
        }

    }

}
