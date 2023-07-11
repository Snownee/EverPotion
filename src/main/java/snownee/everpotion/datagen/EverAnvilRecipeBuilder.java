package snownee.everpotion.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverPotion;
import snownee.everpotion.PotionType;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.util.Util;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class EverAnvilRecipeBuilder implements FinishedRecipe {

	private final ItemStack output;
	protected List<ICondition> conditions = new ArrayList<>();
	private Ingredient left;
	private Ingredient right;
	private int levelCost;
	private int materialCost = 1;
	private ResourceLocation id;

	public EverAnvilRecipeBuilder(ItemStack output) {
		this.output = output;
	}

	public static EverAnvilRecipeBuilder recipe(ItemStack output) {
		return new EverAnvilRecipeBuilder(output);
	}

	public static EverAnvilRecipeBuilder coreRecipe(@Nullable MobEffectInstance effect, PotionType type, float charge) {
		String save;
		if (effect == null) {
			save = "water";
		} else {
			save = Util.trimRL(Registry.MOB_EFFECT.getKey(effect.getEffect())).replace(':', '/');
			if (type == PotionType.SPLASH) {
				save += "_s";
			} else if (type == PotionType.LINGERING) {
				save += "_l";
			}
		}
		return recipe(CoreModule.CORE.get().make(effect, type, charge)).save(new ResourceLocation(EverPotion.ID, save));
	}

	public EverAnvilRecipeBuilder save(ResourceLocation save) {
		id = save;
		return this;
	}

	public void build(Consumer<FinishedRecipe> consumerIn) {
		Preconditions.checkNotNull(id);
		this.validate(id);
		consumerIn.accept(this);
	}

	private void validate(ResourceLocation id) {
		if (left == null) {
			throw new IllegalStateException("Recipe " + id + " : input can't be null");
		}
	}

	public EverAnvilRecipeBuilder levelCost(int cost) {
		this.levelCost = cost;
		return this;
	}

	public EverAnvilRecipeBuilder materialCost(int materialCost) {
		this.materialCost = materialCost;
		return this;
	}

	public EverAnvilRecipeBuilder left(Ingredient left) {
		this.left = left;
		return this;
	}

	public EverAnvilRecipeBuilder right(Ingredient right) {
		this.right = right;
		return this;
	}

	public EverAnvilRecipeBuilder whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	public EverAnvilRecipeBuilder whenModuleLoaded(ResourceLocation moduleId) {
		return withCondition(new ModuleLoadedCondition(moduleId));
	}

	public EverAnvilRecipeBuilder withCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	@Override
	public void serializeRecipeData(JsonObject json) {
		JsonArray inputs = new JsonArray();
		inputs.add(left.toJson());
		inputs.add(right.toJson());
		json.add("item_in", inputs);
		JsonObject itemOut = new JsonObject();
		itemOut.addProperty("item", Registry.ITEM.getKey(output.getItem()).toString());
		if (output.hasTag()) {
			itemOut.add("lychee:tag", LUtil.tagToJson(output.getTag()));
		}
		json.add("item_out", itemOut);
		if (levelCost > 1) {
			json.addProperty("level_cost", levelCost);
		}
		if (materialCost != 1) {
			json.addProperty("material_cost", materialCost);
		}
		if (conditions.isEmpty())
			return;
		JsonArray conds = new JsonArray();
		conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
		json.add("conditions", conds);
	}

	@Override
	public LycheeRecipe.Serializer<AnvilCraftingRecipe> getType() {
		return RecipeSerializers.ANVIL_CRAFTING;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Nullable
	@Override
	public JsonObject serializeAdvancement() {
		return null;
	}

	@Nullable
	@Override
	public ResourceLocation getAdvancementId() {
		return null;
	}

}
