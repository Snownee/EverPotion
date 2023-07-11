package snownee.everpotion.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.inventory.InvHandlerWrapper;
import snownee.kiwi.inventory.container.ModSlot;

public class PlaceMenu extends AbstractContainerMenu {
	private final EverHandler handler;

	public PlaceMenu(int id, Inventory playerInventory) {
		super(CoreModule.MAIN.get(), id);
		Player player = playerInventory.player;
		handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
		if (handler == null) {
			player.openMenu(null);
			return;
		}
		InvHandlerWrapper inventory = new InvHandlerWrapper(handler);

		int xOffset = 2 - EverCommonConfig.maxSlots / 2;
		int xStart = 44 + xOffset * 18;
		for (int j = 0; j < handler.getSlots(); ++j) {
			this.addSlot(new ModSlot(inventory, j, xStart + j * 18, 20));
		}

		for (int l = 0; l < 3; ++l) {
			for (int k = 0; k < 9; ++k) {
				this.addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 109));
		}

	}

	// !! Potential crash if slots changed while player is opening a container
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < this.handler.getSlots()) {
				if (!this.moveItemStackTo(itemstack1, this.handler.getSlots(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, this.handler.getSlots(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if (!playerIn.level.isClientSide) {
			CoreModule.sync((ServerPlayer) playerIn, false);
		}
	}

	public int getSlots() {
		return handler.getSlots();
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return playerIn.isAlive();
	}

	public enum ContainerProvider implements MenuProvider {
		INSTANCE;

		private static final Component TITLE = Component.translatable("container.everpotion.main");

		@Override
		public PlaceMenu createMenu(int windowId, Inventory playerInventory, Player player) {
			return new PlaceMenu(windowId, playerInventory);
		}

		@Override
		public Component getDisplayName() {
			return TITLE;
		}
	}

}
