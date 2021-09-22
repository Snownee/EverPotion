package snownee.everpotion.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
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
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.util.NBTHelper;

public class UnlockSlotItem extends ModItem {

	public UnlockSlotItem() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide) {
			return InteractionResultHolder.fail(stack);
		}
		EverHandler handler = playerIn.getCapability(EverCapabilities.HANDLER).orElse(null);
		if (handler == null) {
			sendMsg((ServerPlayer) playerIn, "noHandler");
			return InteractionResultHolder.fail(stack);
		}
		boolean force = NBTHelper.of(stack).getBoolean("Force");
		int tier = getTier(stack);
		if (!force) {
			if (handler.getSlots() >= EverCommonConfig.maxSlots) {
				sendMsg((ServerPlayer) playerIn, "maxLevel");
				return InteractionResultHolder.fail(stack);
			}
			if (tier > 0) {
				if (handler.getSlots() + 1 < tier) {
					sendMsg((ServerPlayer) playerIn, "tooHigh");
					return InteractionResultHolder.fail(stack);
				}
				if (handler.getSlots() + 1 > tier) {
					sendMsg((ServerPlayer) playerIn, "tooLow");
					return InteractionResultHolder.fail(stack);
				}
			} else {
				tier = handler.getSlots() + 1;
			}
		}
		handler.setSlots(tier);
		// TODO more fancy effects!
		stack.shrink(1);
		worldIn.playSound(null, playerIn.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 1);
		CoreModule.sync((ServerPlayer) playerIn);
		return InteractionResultHolder.consume(stack);
	}

	private static void sendMsg(ServerPlayer player, String translationKey) {
		player.displayClientMessage(new TranslatableComponent("msg.everpotion." + translationKey), true);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (NBTHelper.of(stack).getBoolean("Force")) {
			tooltip.add(new TranslatableComponent("tip.everpotion.force").withStyle(ChatFormatting.RED));
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		int tier = getTier(stack);
		if (tier > 0 && tier <= 4) {
			return getDescriptionId() + "." + tier;
		}
		return getDescriptionId(stack);
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
		if (this.allowdedIn(group)) {
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
