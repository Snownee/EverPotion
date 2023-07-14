package snownee.skillslots.menu;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.kiwi.inventory.container.ModSlot;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.SkillSlotsModule;

public class PlaceMenu extends AbstractContainerMenu {
	private static int OPEN_COUNT;
	public final Slot close;
	private final SkillSlotsHandler handler;

	public PlaceMenu(int id, Inventory playerInventory) {
		super(SkillSlotsModule.PLACE.get(), id);
		Player player = playerInventory.player;
		handler = SkillSlotsHandler.of(player);
		int xStart = 44;

		for (int j = 0; j < SkillSlotsHandler.MAX_SLOTS; ++j) {
			if (j < handler.getContainerSize()) {
				addSlot(new ModSlot(handler, j, xStart + j * 18, 20));
			} else {
				addSlot(new FixedSlot(handler, j, xStart + j * 18, 20));
			}
		}
		ItemStack powderSnow = new ItemStack(Items.POWDER_SNOW_BUCKET);
		Style style = Style.EMPTY.withItalic(false);
		powderSnow.setHoverName(Component.translatable("msg.skillslots.closePlace1").withStyle(style));
		if (player.level.isClientSide && ++OPEN_COUNT % 20 == 2) {
			ListTag lore = new ListTag();
			style = style.withColor(ChatFormatting.GRAY);
			lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("msg.skillslots.closePlace2").withStyle(style))));
			lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("msg.skillslots.closePlace3").withStyle(style))));
			powderSnow.getOrCreateTagElement("display").put("Lore", lore);
		}
		close = addSlot(new FixedSlot(xStart + 4 * 18, 20, powderSnow));

		for (int l = 0; l < 3; ++l) {
			for (int k = 0; k < 9; ++k) {
				addSlot(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 109));
		}

	}

	// !! Potential crash if slots changed while player is opening a container
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index < this.handler.getContainerSize()) {
				if (!this.moveItemStackTo(itemstack1, this.handler.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, this.handler.getContainerSize(), false)) {
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
			handler.dirty = true;
		}
	}

	public int getSlots() {
		return handler.getContainerSize();
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return playerIn.isAlive();
	}

	public enum ContainerProvider implements MenuProvider {
		INSTANCE;

		private static final Component TITLE = Component.translatable("container.skillslots.place");

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
