package snownee.skillslots.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FixedSlot extends Slot {
	private static final SimpleContainer DUMMY = new SimpleContainer(1);
	private ItemStack stack = ItemStack.EMPTY;

	public FixedSlot(int xPosition, int yPosition, ItemStack stack) {
		super(DUMMY, 0, xPosition, yPosition);
		this.stack = stack;
	}

	public FixedSlot(Container container, int index, int xPosition, int yPosition) {
		super(container, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPickup(Player p_40228_) {
		return false;
	}

	@Override
	public boolean mayPlace(ItemStack p_40231_) {
		return false;
	}

	@Override
	public ItemStack getItem() {
		if (container == DUMMY)
			return stack;
		return super.getItem();
	}

	@Override
	public boolean isSameInventory(Slot other) {
		if (container == DUMMY)
			return false;
		return super.isSameInventory(other);
	}
}
