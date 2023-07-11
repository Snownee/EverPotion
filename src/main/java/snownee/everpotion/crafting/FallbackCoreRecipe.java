package snownee.everpotion.crafting;

import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import snownee.everpotion.CoreModule;
import snownee.everpotion.PotionType;
import snownee.lychee.anvil_crafting.AnvilContext;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;

public class FallbackCoreRecipe extends AnvilCraftingRecipe {

	public FallbackCoreRecipe(ResourceLocation id) {
		super(id);
		output = ItemStack.EMPTY;
	}

	@Override
	public boolean matches(AnvilContext inv, Level worldIn) {
		if (!super.matches(inv, worldIn)) {
			return false;
		}
		Potion potion = PotionUtils.getPotion(inv.left);
		if (potion.getEffects().size() != 1) {
			return false;
		}
		MobEffectInstance effect = potion.getEffects().get(0);
		if (effect.getAmplifier() != 0) {
			return false;
		}
		ResourceLocation reg = Registry.POTION.getKey(potion);
		if (!reg.getPath().startsWith("long_") && Registry.POTION.containsKey(new ResourceLocation(reg.getNamespace(), "long_" + reg.getPath()))) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(AnvilContext inv) {
		inv.levelCost = levelCost;
		inv.materialCost = materialCost;
		Potion potion = PotionUtils.getPotion(inv.left);
		MobEffectInstance effect = potion.getEffects().get(0);
		PotionType type = PotionType.NORMAL;
		for (PotionType t : PotionType.values()) {
			if (inv.left.is(t.potionItem)) {
				type = t;
				break;
			}
		}
		effect = new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * type.durationFactor), effect.getAmplifier(), true, true);
		return CoreModule.CORE.get().make(effect, type, 1);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return FallbackRecipeModule.FALLBACK.get();
	}

	public static class Serializer extends LycheeRecipe.Serializer<FallbackCoreRecipe> {

		public Serializer() {
			super(FallbackCoreRecipe::new);
		}

		@Override
		public void fromJson(FallbackCoreRecipe pRecipe, JsonObject pSerializedRecipe) {
			JsonElement itemIn = pSerializedRecipe.get("item_in");
			if (itemIn.isJsonArray()) {
				JsonArray array = itemIn.getAsJsonArray();
				pRecipe.left = Ingredient.fromJson(array.get(0));
				if (array.size() > 0) {
					pRecipe.right = Ingredient.fromJson(array.get(1));
				}
			} else {
				pRecipe.left = Ingredient.fromJson(itemIn);
			}

			JsonElement var10000 = pSerializedRecipe.get("assembling");
			Objects.requireNonNull(pRecipe);
			PostAction.parseActions(var10000, pRecipe::addAssemblingAction);
			pRecipe.levelCost = GsonHelper.getAsInt(pSerializedRecipe, "level_cost", 1);
			Preconditions.checkArgument(pRecipe.levelCost > 0, "level_cost must be greater than 0");
			pRecipe.materialCost = GsonHelper.getAsInt(pSerializedRecipe, "material_cost", 1);
		}

		public void fromNetwork(FallbackCoreRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.left = Ingredient.fromNetwork(pBuffer);
			pRecipe.right = Ingredient.fromNetwork(pBuffer);
			pRecipe.levelCost = pBuffer.readVarInt();
			pRecipe.materialCost = pBuffer.readVarInt();
		}

		public void toNetwork0(FriendlyByteBuf pBuffer, FallbackCoreRecipe pRecipe) {
			pRecipe.left.toNetwork(pBuffer);
			pRecipe.right.toNetwork(pBuffer);
			pBuffer.writeVarInt(pRecipe.levelCost);
			pBuffer.writeVarInt(pRecipe.materialCost);
		}
	}

}
