package snownee.everpotion.crafting;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.item.CoreItem;
import snownee.kiwi.recipe.Simple;
import snownee.kiwi.util.Util;

public class EverAnvilRecipe extends Simple<AnvilContext> {

	protected final Ingredient left;
	protected final Ingredient right;
	protected final int cost;
	protected final int materialCost;
	protected final ProcessingOutput output;

	public EverAnvilRecipe(ResourceLocation id, Ingredient left, Ingredient right, int cost, int materialCost, ItemStack output) {
		super(id);
		this.left = left;
		this.right = right;
		this.cost = cost;
		this.materialCost = materialCost;
		this.output = new ProcessingOutput(output, 1);
	}

	@Override
	public boolean matches(AnvilContext inv, Level worldIn) {
		return left.test(inv.left) && right.test(inv.right);
	}

	@Override
	public ItemStack assemble(AnvilContext inv) {
		inv.cost = cost;
		inv.materialCost = materialCost;
		return getResultItem();
	}

	public Ingredient getLeft() {
		return left;
	}

	public Ingredient getRight() {
		return right;
	}

	@Override
	public ItemStack getResultItem() {
		return output.getStack().copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return CraftingModule.SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return CraftingModule.RECIPE_TYPE;
	}

	@FunctionalInterface
	public interface EverAnvilRecipeFactory<T extends EverAnvilRecipe> {

		T create(ResourceLocation id, Ingredient left, Ingredient right, int cost, int materialCost, ItemStack output);

	}

	public static class Serializer<T extends EverAnvilRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {

		protected final EverAnvilRecipeFactory<T> factory;

		public Serializer(EverAnvilRecipeFactory<T> factory) {
			this.factory = factory;
		}

		@Override
		public T fromJson(ResourceLocation recipeId, JsonObject json) {
			Ingredient left = CraftingHelper.getIngredient(json.get("left"));
			Ingredient right = CraftingHelper.getIngredient(json.get("right"));
			int cost = GsonHelper.getAsInt(json, "cost", 0);
			int materialCost = GsonHelper.getAsInt(json, "materialCost", 1);
			JsonObject outputObj = GsonHelper.getAsJsonObject(json, "output");
			ItemStack output;
			if (outputObj.has("effect")) {
				MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(GsonHelper.getAsString(outputObj, "effect")));
				int duration = GsonHelper.getAsInt(outputObj, "duration", 0) * 20;
				int amplifier = GsonHelper.getAsInt(outputObj, "amplifier", 0);
				MobEffectInstance effectInstance = null;
				if (effect != null) {
					effectInstance = new MobEffectInstance(effect, duration, amplifier, EverCommonConfig.ambient, EverCommonConfig.showParticles, EverCommonConfig.showIcon);
				}
				PotionType type = PotionType.parse(GsonHelper.getAsString(outputObj, "type", "normal"));
				float charge = GsonHelper.getAsFloat(outputObj, "charge", 1);
				output = CoreModule.CORE.make(effectInstance, type, charge);
			} else {
				output = CraftingHelper.getItemStack(outputObj, true);
			}
			return factory.create(recipeId, left, right, cost, materialCost, output);
		}

		public void toJson(JsonObject json, T recipe) {
			json.add("left", recipe.left.toJson());
			json.add("right", recipe.right.toJson());
			if (recipe.cost != 0)
				json.addProperty("cost", recipe.cost);
			if (recipe.materialCost != 1)
				json.addProperty("materialCost", recipe.materialCost);
			ItemStack output = recipe.output.getStack();
			if (output.getItem() == CoreModule.CORE) {
				JsonObject o = new JsonObject();
				MobEffectInstance effect = CoreItem.getEffectInstance(output);
				PotionType type = CoreItem.getPotionType(output);
				float charge = CoreItem.getChargeModifier(output);
				if (effect == null)
					o.addProperty("effect", "none");
				else {
					o.addProperty("effect", Util.trimRL(effect.getEffect().getRegistryName()));
					if (effect.getAmplifier() > 0)
						o.addProperty("amplifier", effect.getAmplifier());
					if (!effect.getEffect().isInstantenous())
						o.addProperty("duration", (int) (effect.getDuration() * type.durationFactor / 20));
				}
				o.addProperty("type", type.toString());
				if (charge != 1)
					o.addProperty("charge", charge);
				json.add("output", o);
			} else {
				json.add("output", recipe.output.serialize());
			}
		}

		@Override
		public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			Ingredient left = Ingredient.fromNetwork(buffer);
			Ingredient right = Ingredient.fromNetwork(buffer);
			int cost = buffer.readVarInt();
			int materialCost = buffer.readVarInt();
			ItemStack output = buffer.readItem();
			return factory.create(recipeId, left, right, cost, materialCost, output);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, T recipe) {
			recipe.left.toNetwork(buffer);
			recipe.right.toNetwork(buffer);
			buffer.writeVarInt(recipe.cost);
			buffer.writeVarInt(recipe.materialCost);
			buffer.writeItem(recipe.getResultItem());
		}

	}

}
