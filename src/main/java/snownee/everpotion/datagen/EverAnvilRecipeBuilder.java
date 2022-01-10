package snownee.everpotion.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverPotion;
import snownee.everpotion.PotionType;
import snownee.everpotion.crafting.CraftingModule;
import snownee.everpotion.crafting.EverAnvilRecipe;
import snownee.kiwi.util.Util;

public class EverAnvilRecipeBuilder implements IFinishedRecipe {

	private Ingredient left;
	private Ingredient right;
	private int cost;
	private int materialCost = 1;
	private final ItemStack output;
	private ResourceLocation id;
	protected List<ICondition> conditions = new ArrayList<>();

	public EverAnvilRecipeBuilder(ItemStack output) {
		this.output = output;
	}

	public static EverAnvilRecipeBuilder recipe(ItemStack output) {
		return new EverAnvilRecipeBuilder(output);
	}

	public static EverAnvilRecipeBuilder coreRecipe(@Nullable EffectInstance effect, PotionType type, float charge) {
		String save;
		if (effect == null) {
			save = "water";
		} else {
			save = Util.trimRL(effect.getPotion().getRegistryName()).replace(':', '/');
			if (type == PotionType.SPLASH) {
				save += "_s";
			} else if (type == PotionType.LINGERING) {
				save += "_l";
			}
		}
		return recipe(CoreModule.CORE.make(effect, type, charge)).save(new ResourceLocation(EverPotion.MODID, save));
	}

	public EverAnvilRecipeBuilder save(ResourceLocation save) {
		id = save;
		return this;
	}

	public void build(Consumer<IFinishedRecipe> consumerIn) {
		if (id == null) {
			ResourceLocation reg = output.getItem().getRegistryName();
			id = new ResourceLocation(reg.getNamespace(), getSerializer().getRegistryName().getPath() + "/" + reg.getPath());
		}
		validate(id);
		consumerIn.accept(this);
	}

	private void validate(ResourceLocation id) {
		if (left == null) {
			throw new IllegalStateException("Recipe " + id + " : input can't be null");
		}
	}

	public EverAnvilRecipeBuilder levelCost(int cost) {
		this.cost = cost;
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

	public EverAnvilRecipeBuilder withCondition(ICondition condition) {
		conditions.add(condition);
		return this;
	}

	@Override
	public void serialize(JsonObject json) {
		EverAnvilRecipe recipe = new EverAnvilRecipe(id, left, right, cost, materialCost, output);
		getSerializer().write(json, recipe);
		if (conditions.isEmpty())
			return;

		JsonArray conds = new JsonArray();
		conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
		json.add("conditions", conds);
	}

	@Override
	public EverAnvilRecipe.Serializer getSerializer() {
		return CraftingModule.SERIALIZER;
	}

	@Override
	public ResourceLocation getID() {
		return id;
	}

	@Nullable
	@Override
	public JsonObject getAdvancementJson() {
		return null;
	}

	@Nullable
	@Override
	public ResourceLocation getAdvancementID() {
		return null;
	}

}
