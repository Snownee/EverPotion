package snownee.everpotion.inventory;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        PotionType type = cache.type;
        if (cache.effect == null && type != PotionType.NORMAL) {
            type = PotionType.SPLASH;
            BlockPos pos = player.getPosition();
            this.extinguishFires(player.world, pos, Direction.DOWN);
            this.extinguishFires(player.world, pos.up(), Direction.DOWN);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.extinguishFires(player.world, pos.offset(direction), direction);
            }
        }

        if (type == PotionType.NORMAL) {
            doEffect(cache.effect, player, player);
        } else if (type == PotionType.SPLASH) {
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(player.getPosition()).grow(4.0D, 2.0D, 4.0D);
            List<LivingEntity> list = player.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);
            if (!list.isEmpty()) {
                for (LivingEntity livingentity : list) {
                    double d0 = player.getDistanceSq(livingentity);
                    if (d0 < 16.0D) {
                        doEffect(cache.effect, player, livingentity);
                    }
                }
            }
        } else { // PotionEntity.makeAreaOfEffectCloud
            AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
            areaeffectcloudentity.setOwner(player);
            areaeffectcloudentity.setRadius(3.0F);
            areaeffectcloudentity.setRadiusOnUse(-0.5F);
            areaeffectcloudentity.setWaitTime(10);
            areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / areaeffectcloudentity.getDuration());
            areaeffectcloudentity.addEffect(new EffectInstance(cache.effect));

            player.world.addEntity(areaeffectcloudentity);
        }
        if (type != PotionType.NORMAL) {
            int i = (cache.effect != null && cache.effect.getPotion().isInstant()) ? 2007 : 2002;
            player.world.playEvent(i, player.getPosition(), cache.color);
        }
    }

    private static void doEffect(EffectInstance effect, PlayerEntity caster, LivingEntity entity) {
        if (effect == null) {
            entity.extinguish();
            if (PotionEntity.WATER_SENSITIVE.test(entity)) {
                entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(entity, caster), 1.0F);
            }
        } else {
            entity.addPotionEffect(effect);
        }
    }

    private void extinguishFires(World world, BlockPos pos, Direction direction) {
        BlockState blockstate = world.getBlockState(pos);
        Block block = blockstate.getBlock();
        if (block == Blocks.FIRE) {
            world.extinguishFire(null, pos.offset(direction), direction.getOpposite());
        } else if (block == Blocks.CAMPFIRE && blockstate.get(CampfireBlock.LIT)) {
            world.playEvent(null, 1009, pos, 0);
            world.setBlockState(pos, blockstate.with(CampfireBlock.LIT, Boolean.FALSE));
        }
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
        public float progress = 100;
        public final int color;

        private Cache(ItemStack stack) {
            effect = CoreItem.getEffectInstance(stack);
            type = CoreItem.getPotionType(stack);
            if (effect != null) {
                color = effect.getPotion().getLiquidColor();
            } else {
                color = 3694022;
            }
        }
    }

}
