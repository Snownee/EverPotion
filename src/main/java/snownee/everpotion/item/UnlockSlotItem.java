package snownee.everpotion.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;

public class UnlockSlotItem extends ModItem {

	public UnlockSlotItem() {
		super(new Item.Properties());
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		EverHandler handler = playerIn.getCapability(EverCapabilities.HANDLER).orElse(null);
		if (handler == null) {
			sendMsg(playerIn, "noHandler");
			return ActionResult.resultFail(stack);
		}
		boolean force = NBTHelper.of(stack).getBoolean("Force");
		int tier = getTier(stack);
		if (!force) {
			if (handler.getSlots() >= EverCommonConfig.maxSlots) {
				sendMsg(playerIn, "maxLevel");
				return ActionResult.resultFail(stack);
			}
			if (tier > 0) {
				if (handler.getSlots() + 1 < tier) {
					sendMsg(playerIn, "tooHigh");
					return ActionResult.resultFail(stack);
				}
				if (handler.getSlots() + 1 > tier) {
					sendMsg(playerIn, "tooLow");
					return ActionResult.resultFail(stack);
				}
			} else {
				tier = handler.getSlots() + 1;
			}
		}
		worldIn.playSound(playerIn, playerIn.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 1);
		if (!worldIn.isRemote) {
			stack.shrink(1);
			handler.setSlots(tier);
			CoreModule.sync((ServerPlayerEntity) playerIn, false);
		} else if (tier > handler.getSlots()) {
			sendMsg(playerIn, "newSlot", ClientHandler.kbUse.func_238171_j_/*getTranslatedKeyMessage*/());
		}
		return ActionResult.func_233538_a_(stack, worldIn.isRemote);
	}

	private static void sendMsg(PlayerEntity player, String translationKey, Object... objects) {
		if (player.world.isRemote) {
			player.sendStatusMessage(new TranslationTextComponent("msg.everpotion." + translationKey, objects), true);
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (NBTHelper.of(stack).getBoolean("Force")) {
			tooltip.add(new TranslationTextComponent("tip.everpotion.force").mergeStyle(TextFormatting.RED));
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		int tier = getTier(stack);
		if (tier > 0 && tier <= 4) {
			return getTranslationKey() + "." + tier;
		}
		return getTranslationKey();
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		switch (getTier(stack)) {
		default:
			return Rarity.COMMON;
		case 2:
			return Rarity.UNCOMMON;
		case 3:
			return Rarity.RARE;
		case 4:
			return Rarity.EPIC;
		}
	}

	public static int getTier(ItemStack stack) {
		return MathHelper.clamp(NBTHelper.of(stack).getInt("Tier"), 0, 4);
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (isInGroup(group)) {
			ItemStack stack = new ItemStack(this);
			NBTHelper data = NBTHelper.of(stack);
			for (int i = 0; i < 2; i++) {
				items.add(stack.copy());
				for (int j = 1; j <= 4; j++) {
					data.setInt("Tier", j);
					items.add(stack.copy());
				}
				stack.getTag().keySet().clear();
				data.setBoolean("Force", true);
			}
		}
	}

}
