package snownee.everpotion.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.everpotion.CoreModule;
import snownee.everpotion.PotionType;

public class FallbackCoreRecipe extends EverAnvilRecipe {

	public FallbackCoreRecipe(ResourceLocation id, Ingredient left, Ingredient right, int cost, int materialCost, ItemStack output) {
		super(id, left, right, materialCost, materialCost, output);
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
		ResourceLocation reg = potion.getRegistryName();
		if (!reg.getPath().startsWith("long_") && ForgeRegistries.POTIONS.containsKey(new ResourceLocation(reg.getNamespace(), "long_" + reg.getPath()))) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack assemble(AnvilContext inv) {
		inv.cost = cost;
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
		return CoreModule.CORE.make(effect, type, 1);
	}

	@Override
	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return FallbackRecipeModule.FALLBACK;
	}

}
