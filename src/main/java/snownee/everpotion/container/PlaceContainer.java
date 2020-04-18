package snownee.everpotion.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.inventory.InvHandlerWrapper;
import snownee.kiwi.inventory.container.ModSlot;

public class PlaceContainer extends Container {
    private final EverHandler handler;

    public PlaceContainer(int id, PlayerInventory playerInventory) {
        super(CoreModule.MAIN, id);
        PlayerEntity player = playerInventory.player;
        handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
        if (handler == null) {
            player.openContainer(null);
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
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.handler.getSlots()) {
                if (!this.mergeItemStack(itemstack1, this.handler.getSlots(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.handler.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote) {
            CoreModule.sync((ServerPlayerEntity) playerIn);
        }
    }

    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.isAlive();
    }

    public enum ContainerProvider implements INamedContainerProvider {
        INSTANCE;

        private static final ITextComponent TITLE = new TranslationTextComponent("container.everpotion.main");

        @Override
        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
            return CoreModule.MAIN.create(windowId, playerInventory);
        }

        @Override
        public ITextComponent getDisplayName() {
            return TITLE;
        }
    }
}
