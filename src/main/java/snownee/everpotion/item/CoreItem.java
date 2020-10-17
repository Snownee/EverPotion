package snownee.everpotion.item;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.EverPotion;
import snownee.everpotion.PotionType;
import snownee.everpotion.container.PlaceContainer;
import snownee.everpotion.crafting.CraftingModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;

public class CoreItem extends ModItem {

    public CoreItem() {
        super(new Item.Properties().maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        EffectInstance effectinstance = getEffectInstance(stack);
        if (effectinstance != null) {
            TextComponent itextcomponent = new TranslationTextComponent(effectinstance.getEffectName());
            Effect effect = effectinstance.getPotion();
            if (effectinstance.getAmplifier() > 0) {
                itextcomponent.appendString(" ").append(new TranslationTextComponent("potion.potency." + effectinstance.getAmplifier()));
            }
            if (effectinstance.getDuration() > 20) {
                itextcomponent.appendString(" (").appendString(EffectUtils.getPotionDurationString(effectinstance, (float) EverCommonConfig.durationFactor)).appendString(")");
            }
            tooltip.add(itextcomponent.mergeStyle(effect.getEffectType().getColor()));
        } else {
            tooltip.add((new TranslationTextComponent("effect.none")).mergeStyle(TextFormatting.GRAY));
        }
        PotionType type = getPotionType(stack);
        if (type != PotionType.NORMAL) {
            tooltip.add(new TranslationTextComponent("tip.everpotion.potionType." + type.toString()).mergeStyle(TextFormatting.GRAY));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nullable
    public static EffectInstance getEffectInstance(ItemStack stack) {
        CompoundNBT tag = stack.getChildTag("Effect");
        return tag == null ? null : EffectInstance.read(tag);
    }

    @Nullable
    public static Effect getEffect(ItemStack stack) {
        EffectInstance instance = getEffectInstance(stack);
        return instance == null ? null : instance.getPotion();
    }

    public static PotionType getPotionType(ItemStack stack) {
        return PotionType.valueOf(NBTHelper.of(stack).getByte("Type"));
    }

    public static float getChargeModifier(ItemStack stack) {
        return NBTHelper.of(stack).getFloat("Charge", 1);
    }

    public ItemStack make(@Nullable EffectInstance effect, PotionType type, float charge) {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putByte("Type", (byte) type.ordinal());
        if (effect != null) {
            stack.getTag().put("Effect", effect.write(new CompoundNBT()));
        }
        if (charge != 1) {
            stack.getTag().putFloat("Charge", charge);
        }
        return stack;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (!this.isInGroup(group)) {
            return;
        }
        if (!Kiwi.isLoaded(new ResourceLocation(EverPotion.MODID, "crafting"))) {
            return;
        }
        RecipeManager manager = CraftingModule.getRecipeManager();
        if (manager == null) {
            return;
        }
        /* off */
        items.addAll(manager.getRecipes(CraftingModule.RECIPE_TYPE).values().stream()
                .map(IRecipe::getRecipeOutput)
                .filter(s -> s.getItem() == CoreModule.CORE)
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
                .collect(Collectors.toList()));
        /* on */
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            playerIn.openContainer(PlaceContainer.ContainerProvider.INSTANCE);
        }
        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

}
