package snownee.everpotion.handler;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.network.CDrinkPacket;
import snownee.kiwi.util.MathUtil;
import snownee.kiwi.util.NBTHelper;

public class EverHandler extends ItemStackHandler {

    private PlayerEntity owner;
    private int slots;
    public final Cache[] caches = new Cache[4];
    public int chargeIndex = -1;
    public int drinkIndex = -1;
    public int drinkTick;
    public float acceleration;

    public EverHandler() {
        this(null);
    }

    public EverHandler(PlayerEntity owner) {
        super(4);
        this.owner = owner;
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Override
    protected void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack.getItem() == CoreModule.CORE) {
            if (caches[slot] != null && caches[slot].matches(stack)) {
                return;
            }
            caches[slot] = new Cache(stack);
        } else {
            caches[slot] = null;
        }
        if (chargeIndex == -1 || slot == chargeIndex) {
            updateCharge();
        }
    }

    private void updateCharge() {
        for (int i = 0; i < caches.length; i++) {
            if (caches[i] == null) {
                continue;
            }
            if (caches[i].progress < EverCommonConfig.refillTime) {
                chargeIndex = i;
                return;
            }
        }
        chargeIndex = -1;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = super.serializeNBT();
        tag.putInt("Slots", slots);
        for (int i = 0; i < caches.length; i++) {
            if (caches[i] == null) {
                continue;
            }
            tag.putFloat("Progress" + i, caches[i].progress);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        NBTHelper data = NBTHelper.of(nbt);
        slots = data.getInt("Slots", EverCommonConfig.beginnerSlots);
        super.deserializeNBT(nbt);
        for (int i = 0; i < slots; i++) {
            onContentsChanged(i);
        }
        for (int i = 0; i < caches.length; i++) {
            if (caches[i] == null) {
                continue;
            }
            this.caches[i].progress = data.getFloat("Progress" + i);
        }
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
        for (int i = 0; i < caches.length; i++) {
            onContentsChanged(i);
            if (caches[i] != null && that.caches[i] != null) {
                this.caches[i].progress = that.caches[i].progress;
            }
        }
        this.chargeIndex = that.chargeIndex;
        this.acceleration = that.acceleration;
    }

    public void tick() {
        acceleration = Math.max(0, acceleration - 0.005f);
        if (chargeIndex != -1) {
            Cache cache = caches[chargeIndex];
            cache.progress = MathHelper.clamp(cache.progress + cache.speed * acceleration, 0, EverCommonConfig.refillTime);
            if (EverCommonConfig.naturallyRefill) {
                cache.progress = MathHelper.clamp(cache.progress + cache.speed, 0, EverCommonConfig.refillTime);
            }
            if (cache.progress == EverCommonConfig.refillTime) {
                updateCharge();
                if (!owner.world.isRemote) {
                    CoreModule.sync((ServerPlayerEntity) owner);
                }
            }
        }
        if (drinkIndex != -1) {
            if (++drinkTick >= EverCommonConfig.drinkDelay) {
                drink(drinkIndex);
                stopDrinking();
                if (chargeIndex == -1) {
                    updateCharge();
                }
            }
        }
    }

    public void startDrinking(int slot) {
        drinkIndex = slot;
        if (owner.world.isRemote) {
            new CDrinkPacket(slot).send();
        }
    }

    public void stopDrinking() {
        drinkIndex = -1;
        drinkTick = 0;
    }

    public void invalidate() {
        owner = null;
    }

    private void drink(int slot) {
        Cache cache = caches[slot];
        cache.progress = 0;
        if (owner.world.isRemote) {
            return;
        }
        PotionType type = cache.type;
        if (cache.effect == null && type != PotionType.NORMAL) {
            type = PotionType.SPLASH;
            BlockPos pos = owner.getPosition();
            this.extinguishFires(owner.world, pos, Direction.DOWN);
            this.extinguishFires(owner.world, pos.up(), Direction.DOWN);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                this.extinguishFires(owner.world, pos.offset(direction), direction);
            }
        }

        if (type == PotionType.NORMAL) {
            doEffect(cache.effect, owner);
        } else if (type == PotionType.SPLASH) {
            AxisAlignedBB axisalignedbb = new AxisAlignedBB(owner.getPosition()).grow(4.0D, 2.0D, 4.0D);
            List<LivingEntity> list = owner.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);
            if (!list.isEmpty()) {
                for (LivingEntity livingentity : list) {
                    double d0 = owner.getDistanceSq(livingentity);
                    if (d0 < 16.0D) {
                        doEffect(cache.effect, livingentity);
                    }
                }
            }
        } else { // PotionEntity.makeAreaOfEffectCloud
            AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(owner.world, owner.getPosX(), owner.getPosY(), owner.getPosZ());
            areaeffectcloudentity.setOwner(owner);
            areaeffectcloudentity.setRadius(3.0F);
            areaeffectcloudentity.setRadiusOnUse(-0.5F);
            areaeffectcloudentity.setWaitTime(10);
            areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / areaeffectcloudentity.getDuration());
            areaeffectcloudentity.addEffect(new EffectInstance(cache.effect));

            owner.world.addEntity(areaeffectcloudentity);
        }
        if (type != PotionType.NORMAL) {
            int i = (cache.effect != null && cache.effect.getPotion().isInstant()) ? 2007 : 2002;
            owner.world.playEvent(i, owner.getPosition(), cache.color);
        }
    }

    private void doEffect(EffectInstance effect, LivingEntity entity) {
        if (effect == null) {
            entity.extinguish();
            if (PotionEntity.WATER_SENSITIVE.test(entity)) {
                entity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(entity, owner), 1.0F);
            }
        } else {
            if (effect.getPotion().isInstant()) {
                effect.getPotion().affectEntity(entity, owner, entity, effect.getAmplifier(), 1.0D);
            } else {
                entity.addPotionEffect(new EffectInstance(effect));
            }
        }
    }

    // Copied from PotionEntity
    private void extinguishFires(World world, BlockPos pos, Direction p_184542_2_) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.isIn(BlockTags.FIRE)) {
            world.removeBlock(pos, false);
        } else if (CampfireBlock.isLit(blockstate)) {
            world.playEvent((PlayerEntity) null, 1009, pos, 0);
            CampfireBlock.extinguish(world, pos, blockstate);
            world.setBlockState(pos, blockstate.with(CampfireBlock.LIT, Boolean.valueOf(false)));
        }
    }

    public boolean canDrink(int slot) {
        if (slot < 0 || slot >= slots) {
            return false;
        }
        return owner != null && drinkIndex == -1 && caches[slot] != null && caches[slot].progress >= EverCommonConfig.refillTime;
    }

    public static final class Cache {
        @Nullable
        public final EffectInstance effect;
        public final PotionType type;
        public float progress;
        public final int color;
        public final ItemStack stack;
        public final float speed;

        private Cache(ItemStack stack) {
            this.stack = stack;
            effect = CoreItem.getEffectInstance(stack);
            type = CoreItem.getPotionType(stack);
            speed = CoreItem.getChargeModifier(stack);
            if (effect != null) {
                int color = effect.getPotion().getLiquidColor();
                Vector3f hsv = MathUtil.RGBtoHSV(color);
                this.color = MathHelper.hsvToRGB(hsv.getX(), hsv.getY(), 1);
            } else {
                color = 4749311; // 3694022;
            }
        }

        private boolean matches(ItemStack stack) {
            return ItemStack.areItemStacksEqual(this.stack, stack);
        }
    }

    public void accelerate(float f) {
        acceleration = Math.min(acceleration + f, 2);
    }

    public void refill() {
        chargeIndex = -1;
        for (int i = 0; i < caches.length; i++) {
            if (caches[i] == null) {
                continue;
            }
            caches[i].progress = EverCommonConfig.refillTime;
        }
        CoreModule.sync((ServerPlayerEntity) owner);
    }

}
