package snownee.skillslots.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;
import snownee.skillslots.SkillSlotsCommonConfig;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.client.SkillSlotsClient;

public class UnlockSlotItem extends ModItem {

	public UnlockSlotItem() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		SkillSlotsHandler handler = SkillSlotsHandler.of(playerIn);
		boolean force = NBTHelper.of(stack).getBoolean("Force");
		int tier = getTier(stack);
		if (!force) {
			if (handler.getContainerSize() >= SkillSlotsCommonConfig.maxSlots) {
				sendMsg(playerIn, "maxLevel");
				if (!worldIn.isClientSide) {
					handler.dirty = true;
				}
				return InteractionResultHolder.fail(stack);
			}
			if (tier > 0) {
				if (handler.getContainerSize() + 1 < tier) {
					sendMsg(playerIn, "tooHigh");
					return InteractionResultHolder.fail(stack);
				}
				if (handler.getContainerSize() + 1 > tier) {
					sendMsg(playerIn, "tooLow");
					return InteractionResultHolder.fail(stack);
				}
			} else {
				tier = handler.getContainerSize() + 1;
			}
		}
		// TODO more fancy effects!
		worldIn.playSound(playerIn, playerIn.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 1);
		if (!worldIn.isClientSide) {
			stack.shrink(1);
			handler.setSlots(tier);
		} else if (tier > handler.getContainerSize()) {
			sendMsg(playerIn, "newSlot", SkillSlotsClient.kbOpen.getTranslatedKeyMessage());
		}
		return InteractionResultHolder.sidedSuccess(stack, worldIn.isClientSide);
	}

	private static void sendMsg(Player player, String translationKey, Object... objects) {
		if (player.level.isClientSide) {
			player.displayClientMessage(Component.translatable("msg.skillslots." + translationKey, objects), true);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (NBTHelper.of(stack).getBoolean("Force")) {
			tooltip.add(Component.translatable("tip.skillslots.force").withStyle(ChatFormatting.RED));
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		int tier = getTier(stack);
		if (tier > 0 && tier <= 4) {
			return getDescriptionId() + "." + tier;
		}
		return getDescriptionId();
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
		return Mth.clamp(NBTHelper.of(stack).getInt("Tier"), 0, 4);
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (this.allowedIn(group)) {
			ItemStack stack = new ItemStack(this);
			NBTHelper data = NBTHelper.of(stack);
			for (int i = 0; i < 2; i++) {
				items.add(stack.copy());
				for (int j = 1; j <= 4; j++) {
					data.setInt("Tier", j);
					items.add(stack.copy());
				}
				stack.getTag().getAllKeys().clear();
				data.setBoolean("Force", true);
			}
		}
	}

}
