package snownee.skillslots;

import java.util.BitSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.MathUtil;
import snownee.skillslots.client.SkillSlotsClient;
import snownee.skillslots.duck.SkillSlotsPlayer;
import snownee.skillslots.network.CStartUsingPacket;
import snownee.skillslots.skill.Skill;

public class SkillSlotsHandler extends SimpleContainer {

	public static final int MAX_SLOTS = 4;
	public final NonNullList<Skill> skills = NonNullList.withSize(MAX_SLOTS, Skill.EMPTY);
	public BitSet toggles = new BitSet(MAX_SLOTS);
	public int chargeIndex = -1;
	public int useIndex = -1;
	public int useTick;
	public float acceleration;
	public boolean dirty;
	private Player owner;
	private int slots;

	public SkillSlotsHandler() {
		super(MAX_SLOTS);
	}

	public SkillSlotsHandler(Player owner) {
		this();
		this.owner = owner;
	}

	@Nullable
	public static SkillSlotsHandler of(@Nullable LivingEntity player) {
		if (player instanceof SkillSlotsPlayer epp) {
			return epp.skillslots$getHandler();
		}
		return null;
	}

	@NotNull
	public static SkillSlotsHandler of(Player player) {
		return ((SkillSlotsPlayer) player).skillslots$getHandler();
	}

	@Override
	public int getContainerSize() {
		return slots;
	}

	public void setSlots(int slots) {
		this.slots = slots = Mth.clamp(slots, 0, SkillSlotsCommonConfig.maxSlots);
		if (useIndex >= slots) {
			useIndex = -1;
		}
		toggles.clear(slots, MAX_SLOTS);
		updateCharge();
		dirty = true;
	}

	@Override
	public void setChanged() {
		super.setChanged();
		for (int i = 0; i < MAX_SLOTS; i++) {
			ItemStack stack = getItem(i);
			if (ItemStack.matches(stack, skills.get(i).item)) {
				continue;
			}
			Skill skill = SkillSlots.createSkill(stack);
			skills.set(i, skill);
			toggles.clear(i);
			if (owner != null && owner.level.isClientSide && !skill.isEmpty()) {
				SkillSlotsClient.getClientHandler(skill).pickColor(skill, color -> {
					Vector3f hsv = MathUtil.RGBtoHSV(color);
					if (Float.isNaN(hsv.x())) {
						return 0xCCCCCC;
					}
					return Mth.hsvToRgb(hsv.x(), hsv.y(), 0.9F);
				});
			}
			if (i == chargeIndex) {
				updateCharge();
			}
			dirty = true;
		}
		if (chargeIndex == -1) {
			updateCharge();
			if (chargeIndex != -1) {
				dirty = true;
			}
		}
	}

	public void updateCharge() {
		for (int i = 0; i < slots; i++) {
			Skill skill = skills.get(i);
			int chargeDuration = skill.getChargeDuration(owner);
			if (chargeDuration == 0) {
				continue;
			}
			if (skill.progress < chargeDuration) {
				chargeIndex = i;
				dirty = true;
				return;
			}
		}
		if (chargeIndex != -1) {
			chargeIndex = -1;
			dirty = true;
		}
	}

	@Override
	public @NotNull ListTag createTag() {
		ListTag listTag = new ListTag();
		for (int i = 0; i < MAX_SLOTS; i++) {
			ItemStack stack = getItem(i);
			if (!stack.isEmpty()) {
				CompoundTag tag = new CompoundTag();
				tag.putByte("Slot", (byte) i);
				stack.save(tag);
				listTag.add(tag);
			}
		}
		return listTag;
	}

	@Override
	public void fromTag(ListTag listTag) {
		for (int i = 0; i < MAX_SLOTS; i++) {
			removeItemNoUpdate(i);
		}
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag tag = listTag.getCompound(i);
			int j = tag.getByte("Slot") & 255;
			if (j < MAX_SLOTS) {
				setItem(j, ItemStack.of(tag));
			}
		}
	}

	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.put("Items", createTag());
		tag.putInt("UnlockedSlots", slots - SkillSlotsCommonConfig.beginnerSlots);
		for (int i = 0; i < MAX_SLOTS; i++) {
			Skill skill = skills.get(i);
			if (skill.getChargeDuration(owner) > 0) {
				tag.putFloat("Progress" + i, skill.progress);
			}
		}
		byte[] bytes = toggles.toByteArray();
		if (bytes.length > 0) {
			tag.putByteArray("Toggles", bytes);
		}
		tag.putFloat("Acceleration", acceleration);
		return tag;
	}

	public void deserializeNBT(CompoundTag data) {
		if (data.contains("Items", Tag.TAG_LIST)) {
			fromTag(data.getList("Items", Tag.TAG_COMPOUND));
		}
		if (data.contains("UnlockedSlots", Tag.TAG_INT)) {
			slots = Mth.clamp(SkillSlotsCommonConfig.beginnerSlots + data.getInt("UnlockedSlots"), 0, SkillSlotsCommonConfig.maxSlots);
		} else {
			slots = Math.max(SkillSlotsCommonConfig.beginnerSlots, data.getInt("Slots"));
		}
		setChanged();
		for (int i = 0; i < MAX_SLOTS; i++) {
			Skill skill = skills.get(i);
			if (skill.getChargeDuration(owner) > 0) {
				skill.progress = data.getFloat("Progress" + i);
			}
		}
		if (data.contains("Toggles", Tag.TAG_BYTE_ARRAY)) {
			toggles = BitSet.valueOf(data.getByteArray("Toggles"));
		}
		acceleration = data.getFloat("Acceleration");
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			return true;
		}
		if (!stack.is(SkillSlotsModule.SKILL)) {
			return false;
		}
		Skill skill = SkillSlots.createSkill(stack);
		if (skill.isEmpty()) {
			return false;
		}
		for (int i = 0; i < MAX_SLOTS; i++) {
			if (i == slot) {
				continue;
			}
			Skill skillIn = skills.get(i);
			if (!skill.isEmpty() && skillIn.isConflicting(skill)) {
				return false;
			}
		}
		return true;
	}

	public void copyFrom(SkillSlotsHandler that) {
		slots = that.slots;
		for (int i = 0; i < MAX_SLOTS; i++) {
			setItem(i, that.getItem(i).copy());
			Skill skill = skills.get(i);
			if (!skill.isEmpty()) {
				skill.progress = that.skills.get(i).progress;
			}
		}
		this.chargeIndex = that.chargeIndex;
		this.toggles = (BitSet) that.toggles.clone();
		this.acceleration = that.acceleration;
		setChanged();
	}

	public void tick() {
		acceleration = Math.max(0, acceleration - 0.005f);
		if (chargeIndex != -1) {
			Skill skill = skills.get(chargeIndex);
			int chargeDuration = skill.getChargeDuration(owner);
			if (chargeDuration == 0) {
				updateCharge();
				return;
			}
			float speed = skill.getChargeSpeed(owner);
			skill.progress = Mth.clamp(skill.progress + speed * acceleration, 0, chargeDuration);
			if (SkillSlotsCommonConfig.naturallyCharging) {
				skill.progress = Mth.clamp(skill.progress + speed, 0, chargeDuration);
			}
			if (skill.progress == chargeDuration) {
				updateCharge();
				playChargeCompleteSound(skill);
			}
		}
		if (useIndex != -1) {
			Skill skill = skills.get(useIndex);
			if (skill.canBeToggled()) {
				useTick = 0;
				return;
			}
			if (++useTick >= skill.getUseDuration()) {
				skill.progress = 0;
				skill.finishUsing(owner, useIndex);
				useTick = 0;
				useIndex = -1;
				if (chargeIndex == -1) {
					updateCharge();
				}
			}
		}
		if (dirty && owner instanceof ServerPlayer) {
			SkillSlotsModule.sync((ServerPlayer) owner);
		}
	}

	public void startUsing(int slot) {
		Skill skill = skills.get(slot);
		if (skill.isEmpty()) {
			return;
		}
		if (skill.canBeToggled()) {
			toggles.flip(slot);
			skill.onToggled(owner, this, slot);
			if (owner.level.isClientSide) {
				CStartUsingPacket.send(slot);
			}
			return;
		}
		skill.startUsing(owner, slot);
		if (skill.getUseDuration() == 0) {
			skill.finishUsing(owner, slot);
		} else {
			// TODO reduce player speed
			useIndex = slot;
		}
		if (owner.level.isClientSide) {
			CStartUsingPacket.send(slot);
		}
	}

	public void abortUsing() {
		if (useIndex == -1) {
			return;
		}
		Skill skill = skills.get(useIndex);
		skill.abortUsing(owner, useIndex);
		useIndex = -1;
		useTick = 0;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public boolean canUseSlot(int slot) {
		if (slot < 0 || slot >= slots) {
			return false;
		}
		if (owner != null && useIndex == -1) {
			Skill skill = skills.get(slot);
			if (skill.canBeToggled()) {
				return true;
			}
			return skill.canUse(owner);
		}
		return false;
	}

	public void accelerate(float f) {
		acceleration = Math.min(acceleration + f, 2);
		dirty = true;
	}

	public void setAll(boolean fill) {
		for (Skill skill : skills) {
			int duration = skill.getChargeDuration(owner);
			if (duration == 0) {
				continue;
			}
			if (fill && skill.progress < duration) {
				playChargeCompleteSound(skill);
			}
			skill.progress = fill ? duration : 0;
		}
		updateCharge();
	}

	private void playChargeCompleteSound(Skill skill) {
		if (owner.level.isClientSide || !SkillSlotsCommonConfig.playChargeCompleteSound) {
			return;
		}
		@Nullable Either<SoundEvent, ResourceLocation> sound = skill.getChargeCompleteSound();
		if (sound != null) {
			sound.ifLeft(s -> owner.playNotifySound(s, SoundSource.PLAYERS, 0.5F, 1));
			sound.ifRight(s -> ((ServerPlayer) owner).connection.send(new ClientboundCustomSoundPacket(s, SoundSource.PLAYERS, owner.position(), 0.5F, 1, owner.level.getRandom().nextLong())));
		}
	}

}
