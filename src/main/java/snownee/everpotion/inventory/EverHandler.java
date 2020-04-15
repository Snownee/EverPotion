package snownee.everpotion.inventory;

import javax.annotation.Nullable;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.items.ItemStackHandler;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.item.CoreItem;
import snownee.kiwi.util.NBTHelper;

public class EverHandler extends ItemStackHandler {

    private int slots;
    public final Cache[] caches = new Cache[4];
    private int chargingSlot = -1;

    public EverHandler() {
        super(4);
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    protected void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack.getItem() == CoreModule.CORE) {
            caches[slot] = new Cache(stack);
        } else {
            caches[slot] = null;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = super.serializeNBT();
        tag.putInt("Slots", slots);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        slots = NBTHelper.of(nbt).getInt("Slots", EverCommonConfig.beginnerSlots);
        super.deserializeNBT(nbt);
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.getItem() != CoreModule.CORE || slot >= slots) {
            return false;
        }
        Effect effect = CoreItem.getEffect(stack);
        for (ItemStack stackIn : stacks) {
            if (!stackIn.isEmpty() && stackIn.getItem() == CoreModule.CORE && CoreItem.getEffect(stackIn) == effect) {
                return false;
            }
        }
        return true;
    }

    public void copyFrom(EverHandler that) {
        this.setSlots(that.getSlots());
        this.stacks = that.stacks;
    }

    public void tick() {

    }

    public void drink(ServerPlayerEntity player, int slot) {
        Cache cache = caches[slot];
        if (cache.effect != null) {

        }
        //        switch (cache.type) {
        //        case NORMAL:
        //            player.addPotionEffect(new EffectInstance(cache.effect));
        //            break;
        //        case SPLASH:
        //            PotionEntity
        //            break;
        //        case LINGERING:
        //            break;
        //        }
    }

    public boolean canDrink(int slot) {
        if (slot < 0 || slot >= slots) {
            return false;
        }
        return caches[slot] != null && caches[slot].progress >= 100;
    }

    public static final class Cache {
        @Nullable
        public final EffectInstance effect;
        public final PotionType type;
        public float progress;
        /** client only */
        public final int color;

        private Cache(ItemStack stack) {
            effect = CoreItem.getEffectInstance(stack);
            type = CoreItem.getPotionType(stack);
            color = 0;
        }
    }

}
