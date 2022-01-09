package snownee.everpotion.handler;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.client.ClientHandler;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.network.CDrinkPacket;
import snownee.everpotion.network.SSplashPacket;
import snownee.kiwi.util.MathUtil;
import snownee.kiwi.util.NBTHelper;

public class EverHandler extends ItemStackHandler {

	private Player owner;
	private int slots;
	public final Cache[] caches = new Cache[4];
	public int chargeIndex = -1;
	public int drinkIndex = -1;
	public int tipIndex = -1;
	public int drinkTick;
	public float acceleration;

	public EverHandler() {
		this(null);
	}

	public EverHandler(Player owner) {
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
		if (slot == tipIndex) {
			tipIndex = -1;
		}
	}

	private void updateCharge() {
		for (int i = 0; i < slots; i++) {
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
	public CompoundTag serializeNBT() {
		CompoundTag tag = super.serializeNBT();
		tag.putInt("Slots", slots);
		for (int i = 0; i < caches.length; i++) {
			if (caches[i] == null) {
				continue;
			}
			tag.putFloat("Progress" + i, caches[i].progress);
		}
		tag.putInt("Tip", tipIndex);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
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
		tipIndex = data.getInt("Tip", -1);
	}

	public void setSlots(int slots) {
		this.slots = slots;
		drinkIndex = -1;
		tipIndex = -1;
		updateCharge();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if (stack.getItem() != CoreModule.CORE || slot >= slots) {
			return false;
		}
		MobEffect effect = CoreItem.getEffect(stack);
		for (ItemStack stackIn : stacks) {
			if (!stackIn.isEmpty() && stackIn.getItem() == CoreModule.CORE && CoreItem.getEffect(stackIn) == effect) {
				return false;
			}
		}
		return true;
	}

	public void copyFrom(EverHandler that) {
		slots = that.slots;
		stacks = that.stacks;
		for (int i = 0; i < caches.length; i++) {
			onContentsChanged(i);
			if (caches[i] != null && that.caches[i] != null) {
				this.caches[i].progress = that.caches[i].progress;
			}
		}
		this.chargeIndex = that.chargeIndex;
		this.tipIndex = that.tipIndex;
		this.acceleration = that.acceleration;
	}

	public void tick() {
		acceleration = Math.max(0, acceleration - 0.005f);
		if (chargeIndex != -1) {
			Cache cache = caches[chargeIndex];
			if (cache == null) {
				updateCharge();
				return;
			}
			float oProgress = cache.progress;
			cache.progress = Mth.clamp(cache.progress + cache.speed * acceleration, 0, EverCommonConfig.refillTime);
			if (EverCommonConfig.naturallyRefill) {
				cache.progress = Mth.clamp(cache.progress + cache.speed, 0, EverCommonConfig.refillTime);
			}
			if (cache.progress == EverCommonConfig.refillTime) {
				updateCharge();
				if (!owner.level.isClientSide && cache.progress != oProgress) {
					CoreModule.sync((ServerPlayer) owner, true);
				}
			}
		}
		if (drinkIndex != -1) {
			Cache cache = caches[drinkIndex];
			if (cache == null) {
				return;
			}
			if (cache.type == PotionType.LINGERING) {
				drinkTick = 0;
				return;
			}
			if (++drinkTick >= EverCommonConfig.drinkDelay) {
				use(drinkIndex);
				stopDrinking();
				if (chargeIndex == -1) {
					updateCharge();
				}
			}
		}
	}

	public void startDrinking(int slot) {
		Cache cache = caches[slot];
		if (cache.type == PotionType.LINGERING) {
			if (tipIndex == slot) {
				tipIndex = -1;
			} else {
				tipIndex = slot;
			}
		} else {
			drinkIndex = slot;
		}
		if (owner.level.isClientSide) {
			CDrinkPacket.send(slot);

			if (cache.type == PotionType.LINGERING) {
				ClientHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
			} else if (EverCommonConfig.drinkDelay < 40) {
				ClientHandler.playSound(CoreModule.CHARGE_SHORT_SOUND);
			} else {
				ClientHandler.playSound(CoreModule.CHARGE_LONG_SOUND);
			}
		}
	}

	public void stopDrinking() {
		drinkIndex = -1;
		drinkTick = 0;
		// stop sound?
	}

	public void invalidate() {
		owner = null;
	}

	private void use(int slot) {
		Cache cache = caches[slot];
		cache.progress = 0;
		if (owner.level.isClientSide) {
			return;
		}

		PotionType type = cache.type;
		if (type == PotionType.NORMAL) {
			doEffect(cache.effect, owner);
			owner.level.playSound(null, owner, CoreModule.USE_NORMAL_SOUND, SoundSource.PLAYERS, 1, 1);
		} else if (type == PotionType.SPLASH) {
			if (cache.effect == null) {
				BlockPos pos = owner.blockPosition();
				this.extinguishFires(owner.level, pos, Direction.DOWN);
				this.extinguishFires(owner.level, pos.above(), Direction.DOWN);

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					this.extinguishFires(owner.level, pos.relative(direction), direction);
				}
			}
			AABB axisalignedbb = new AABB(owner.blockPosition()).inflate(4.0D, 2.0D, 4.0D);
			List<LivingEntity> list = owner.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
			if (!list.isEmpty()) {
				for (LivingEntity livingentity : list) {
					double d0 = owner.distanceToSqr(livingentity);
					if (d0 < 16.0D) {
						doEffect(cache.effect, livingentity);
					}
				}
			}
			boolean instant = cache.effect != null && cache.effect.getEffect().isInstantenous();
			SSplashPacket.send(owner, cache.color, instant);
		}
	}

	private void doEffect(MobEffectInstance effect, LivingEntity entity) {
		if (effect == null) {
			entity.clearFire();
			if (ThrownPotion.WATER_SENSITIVE.test(entity)) {
				entity.hurt(DamageSource.indirectMagic(entity, owner), 1.0F);
			}
		} else {
			if (effect.getEffect().isInstantenous()) {
				effect.getEffect().applyInstantenousEffect(entity, owner, entity, effect.getAmplifier(), 1.0D);
			} else {
				entity.addEffect(new MobEffectInstance(effect));
			}
		}
	}

	// Copied from ThrownPotion
	private void extinguishFires(Level world, BlockPos pos, Direction p_184542_2_) {
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.is(BlockTags.FIRE)) {
			world.removeBlock(pos, false);
		} else if (CampfireBlock.isLitCampfire(blockstate)) {
			world.levelEvent(null, 1009, pos, 0);
			CampfireBlock.dowse(owner, world, pos, blockstate);
			world.setBlockAndUpdate(pos, blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
		}
	}

	public boolean canUseSlot(int slot, boolean selectOnly) {
		if (slot < 0 || slot >= slots) {
			return false;
		}
		if (owner != null && drinkIndex == -1 && caches[slot] != null) {
			Cache cache = caches[slot];
			if (cache.type == PotionType.LINGERING) {
				return selectOnly || cache.progress >= EverCommonConfig.tipArrowTimeCost;
			} else {
				return cache.progress >= EverCommonConfig.refillTime;
			}
		}
		return false;
	}

	public AbstractArrow tryTipArrow(Level worldIn, ItemStack stack) {
		if (!worldIn.isClientSide && tipIndex != -1 && caches[tipIndex].progress >= EverCommonConfig.tipArrowTimeCost && caches[tipIndex].effect != null) {
			EverArrow arrow = new EverArrow(worldIn, owner);
			arrow.addEffect(new MobEffectInstance(caches[tipIndex].effect));
			caches[tipIndex].progress -= EverCommonConfig.tipArrowTimeCost;
			updateCharge();
			CoreModule.sync((ServerPlayer) owner, false);
			return arrow;
		}
		return null;
	}

	public static final class Cache {
		@Nullable
		public final MobEffectInstance effect;
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
				int color = effect.getEffect().getColor();
				Vector3f hsv = MathUtil.RGBtoHSV(color);
				this.color = Mth.hsvToRgb(hsv.x(), hsv.y(), 1);
			} else {
				color = 4749311; // 3694022;
			}
		}

		private boolean matches(ItemStack stack) {
			return ItemStack.matches(this.stack, stack);
		}
	}

	public void accelerate(float f) {
		acceleration = Math.min(acceleration + f, 2);
	}

	public void setAll(int time) {
		chargeIndex = -1;
		for (int i = 0; i < caches.length; i++) {
			if (caches[i] == null) {
				continue;
			}
			caches[i].progress = time;
		}
		CoreModule.sync((ServerPlayer) owner, time == EverCommonConfig.refillTime);
	}

}
