package snownee.everpotion.crafting;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import snownee.kiwi.crafting.EmptyInventory;

public class AnvilContext extends EmptyInventory {
	@Nonnull
	public final ItemStack left; // The left side of the input
	@Nonnull
	public final ItemStack right; // The right side of the input
	public final String name; // The name to set the item, if the user specified one.
	public int cost; // The base cost, set this to change it if output != null
	public int materialCost = 1; // The number of items from the right slot to be consumed during the repair. Leave as 0 to consume the entire stack.

	public AnvilContext(ItemStack left, ItemStack right, String name) {
		this.left = left;
		this.right = right;
		this.name = name;
	}

	public AnvilContext(AnvilUpdateEvent event) {
		this.left = event.getLeft();
		this.right = event.getRight();
		this.name = event.getName();
	}
}
