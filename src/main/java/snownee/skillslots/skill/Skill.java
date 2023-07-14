package snownee.skillslots.skill;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.SkillSlotsModule;
import snownee.skillslots.util.CommonProxy;

public class Skill {
	public static final Skill EMPTY = new Skill(ItemStack.EMPTY);
	public final ItemStack item;
	public float progress;
	public float speed = 1;
	public int color = 0xCCCCCC;

	public Skill(ItemStack item) {
		this.item = item.copy();
	}

	public void startUsing(Player player, int slot) {
	}

	/**
	 * Only modify skill.item's nbt here. Or refresh skills in the handler yourself.
	 */
	public void finishUsing(Player player, int slot) {
		Level level = player.level;
		ServerPlayer serverPlayer = level.isClientSide ? null : (ServerPlayer) player;
		ItemStack prev = player.getMainHandItem();
		ItemStack copy = this.item.copy();
		Inventory inventory = player.getInventory();
		inventory.items.set(inventory.selected, copy); // avoid emit game event
		player.setItemInHand(InteractionHand.MAIN_HAND, copy);

		Vec3 start = player.getEyePosition(1);
		Vec3 end = start.add(player.getLookAngle().scale(CommonProxy.getReachDistance(player)));
		HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.MISS) {
			end = hit.getLocation();
		}
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, end, player.getBoundingBox().expandTowards(end).inflate(1), entity -> entity != player);
		if (entityHit != null) {
			hit = entityHit;
		}

		InteractionResult result = InteractionResult.PASS;
		switch (hit.getType()) {
			case BLOCK -> {
				if (level.isClientSide) {
					UseOnContext ctx = new UseOnContext(player, InteractionHand.MAIN_HAND, (BlockHitResult) hit);
					result = copy.useOn(ctx);
				} else {
					result = serverPlayer.gameMode.useItemOn(serverPlayer, level, copy, InteractionHand.MAIN_HAND, (BlockHitResult) hit);
				}
			}
			case ENTITY -> {
				if (!level.isClientSide) {
					ServerboundInteractPacket.createInteractionPacket(entityHit.getEntity(), player.isSecondaryUseActive(), InteractionHand.MAIN_HAND).handle(serverPlayer.connection);
					result = InteractionResult.CONSUME;
				}
			}
			case MISS -> {
			}
		}

		if (result == InteractionResult.PASS && !copy.isEmpty()) {
			if (level.isClientSide) {
				copy.use(level, player, InteractionHand.MAIN_HAND);
			} else {
				serverPlayer.gameMode.useItem(serverPlayer, level, copy, InteractionHand.MAIN_HAND);
				if (getUseDuration() > 0) {
					copy.finishUsingItem(level, player);
				}
			}
		}

		ItemStack now = player.getMainHandItem();
		if (!ItemStack.matches(item, now)) {
			SkillSlotsHandler handler = SkillSlotsHandler.of(player);
			if (handler.canPlaceItem(slot, now)) {
				handler.setItem(slot, now);
			} else {
				handler.setItem(slot, ItemStack.EMPTY);
				player.addItem(now);
			}
		}
		inventory.items.set(inventory.selected, prev);
	}

	public void abortUsing(Player player, int slot) {
	}

	public final Component getDisplayName() {
		if (item.isEmpty()) {
			return Component.empty();
		}
		if (item.hasCustomHoverName()) {
			return item.getHoverName();
		}
		return getDisplayNameInternal();
	}

	protected Component getDisplayNameInternal() {
		List<MobEffectInstance> effects = PotionUtils.getMobEffects(item);
		if (effects.size() == 1) {
			return effects.get(0).getEffect().getDisplayName();
		}
		return item.getHoverName();
	}

	public Component getActionDescription() {
		return getDisplayName();
	}

	public final boolean isEmpty() {
		return this == EMPTY;
	}

	public int getUseDuration() {
		CompoundTag tag = item.getTagElement("SkillSlots");
		if (tag != null && tag.contains("UseDuration", Tag.TAG_INT)) {
			return tag.getInt("UseDuration");
		}
		if (item.getItem() instanceof InstrumentItem) {
			return 0;
		}
		return item.getUseDuration();
	}

	public boolean canBeToggled() {
		CompoundTag tag = item.getTagElement("SkillSlots");
		if (tag != null && tag.contains("CanBeToggled", Tag.TAG_BYTE)) {
			return tag.getBoolean("CanBeToggled");
		}
		return false;
	}

	public void onToggled(Player player, SkillSlotsHandler handler, int slot) {
	}

	public int getChargeDuration(Player player) {
		return 0;
	}

	public boolean canUse(Player player) {
		if (getClass() == Skill.class && player.getCooldowns().isOnCooldown(item.getItem())) {
			return false;
		}
		return progress >= getChargeDuration(player);
	}

	public float getDisplayChargeProgress(Player player, float pTicks) {
		int duration = getChargeDuration(player);
		if (duration == 0) {
			float progress = player.getCooldowns().getCooldownPercent(item.getItem(), pTicks);
			return 1 - progress;
		}
		return Math.min(1, progress / duration);
	}

	public float getChargeSpeed(Player player) {
		return speed;
	}

	public boolean isConflicting(Skill that) {
		return false;
	}

	@Nullable
	public Either<SoundEvent, ResourceLocation> getChargeCompleteSound() {
		CompoundTag tag = item.getTagElement("SkillSlots");
		if (tag != null && tag.contains("ChargeCompleteSound", Tag.TAG_STRING)) {
			String s = tag.getString("ChargeCompleteSound");
			if (s.isEmpty()) {
				return null;
			}
			ResourceLocation id = ResourceLocation.tryParse(s);
			if (id != null) {
				return Either.right(id);
			}
		}
		return Either.left(SkillSlotsModule.POTION_CHARGE_COMPLETE_SOUND.get());
	}
}
