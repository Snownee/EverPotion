package snownee.everpotion.item;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.EverPotion;
import snownee.everpotion.PotionType;
import snownee.everpotion.menu.PlaceMenu;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;
import snownee.lychee.util.LUtil;

public class CoreItem extends ModItem {

	public CoreItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Nullable
	public static MobEffectInstance getEffectInstance(ItemStack stack) {
		CompoundTag tag = stack.getTagElement("Effect");
		return tag == null ? null : MobEffectInstance.load(tag);
	}

	@Nullable
	public static MobEffect getEffect(ItemStack stack) {
		MobEffectInstance instance = getEffectInstance(stack);
		return instance == null ? null : instance.getEffect();
	}

	public static PotionType getPotionType(ItemStack stack) {
		return PotionType.valueOf(NBTHelper.of(stack).getByte("Type"));
	}

	public static float getChargeModifier(ItemStack stack) {
		return NBTHelper.of(stack).getFloat("Charge", 1);
	}

	//PotionItem
	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		MobEffectInstance effectinstance = getEffectInstance(stack);
		if (effectinstance != null) {
			MutableComponent itextcomponent = Component.translatable(effectinstance.getDescriptionId());
			MobEffect effect = effectinstance.getEffect();
			if (effectinstance.getAmplifier() > 0) {
				itextcomponent = Component.translatable("potion.withAmplifier", itextcomponent, Component.translatable("potion.potency." + effectinstance.getAmplifier()));
			}
			if (effectinstance.getDuration() > 20) {
				itextcomponent = Component.translatable("potion.withDuration", itextcomponent, MobEffectUtil.formatDuration(effectinstance, (float) EverCommonConfig.durationFactor));
			}
			tooltip.add(itextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
		} else {
			tooltip.add((Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		}
		PotionType type = getPotionType(stack);
		if (type != PotionType.NORMAL) {
			tooltip.add(Component.translatable(type.getDescKey()).withStyle(ChatFormatting.GRAY));
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	public ItemStack make(@Nullable MobEffectInstance effect, PotionType type, float charge) {
		ItemStack stack = new ItemStack(this);
		stack.getOrCreateTag().putByte("Type", (byte) type.ordinal());
		if (effect != null) {
			CompoundTag effectTag = effect.save(new CompoundTag());
			effectTag.remove("Ambient");
			effectTag.remove("ShowParticles");
			effectTag.remove("ShowIcon");
			effectTag.remove("CurativeItems");
			stack.getTag().put("Effect", effectTag);
		}
		if (charge != 1) {
			stack.getTag().putFloat("Charge", charge);
		}
		return stack;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (!this.allowedIn(group)) {
			return;
		}
		if (!EverPotion.hasLychee) {
			return;
		}
		RecipeType<? extends Recipe<?>> recipeType = Registry.RECIPE_TYPE.get(new ResourceLocation("lychee:anvil_crafting"));
		/* off */
		items.addAll(LUtil.recipes(recipeType).stream()
				.map(Recipe::getResultItem)
				.filter(CoreModule.CORE::is)
				.sorted((a, b) -> {
					String effectA = Objects.toString(getEffect(a));
					String effectB = Objects.toString(getEffect(b));
					int i = effectA.compareTo(effectB);
					if (i != 0) {
						return i;
					}
					PotionType typeA = getPotionType(a);
					PotionType typeB = getPotionType(b);
					return typeA.compareTo(typeB);
				})
				.toList());
		/* on */
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!worldIn.isClientSide) {
			playerIn.openMenu(PlaceMenu.ContainerProvider.INSTANCE);
		}
		return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
	}

}
