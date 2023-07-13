package snownee.everpotion.skill;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverCommonConfig;
import snownee.everpotion.PotionType;
import snownee.everpotion.entity.EverArrow;
import snownee.everpotion.item.CoreItem;
import snownee.everpotion.network.SSplashPacket;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.SkillSlotsModule;
import snownee.skillslots.client.SkillSlotsClient;
import snownee.skillslots.skill.Skill;

public class PotionCoreSkill extends Skill {
	@Nullable
	public final MobEffectInstance effect;
	public final PotionType type;

	public PotionCoreSkill(ItemStack stack) {
		super(stack);
		effect = CoreItem.getEffectInstance(stack);
		type = CoreItem.getPotionType(stack);
		speed = CoreItem.getChargeModifier(stack);
	}

	public static MobEffectInstance copyEffectWithSettings(MobEffectInstance effect) {
		return new MobEffectInstance(effect.getEffect(), (int) (effect.getDuration() * EverCommonConfig.durationFactor), effect.getAmplifier(), EverCommonConfig.ambient, EverCommonConfig.showIcon, EverCommonConfig.showParticles);
	}

	public static AbstractArrow tryTipArrow(ServerPlayer player, Level worldIn, ItemStack stack) {
		SkillSlotsHandler handler = SkillSlotsHandler.of(player);
		PotionCoreSkill skill = findTipIndex(handler);
		if (skill != null) {
			EverArrow arrow = new EverArrow(worldIn, player);
			arrow.addEffect(copyEffectWithSettings(Objects.requireNonNull(skill.effect)));
			skill.progress -= EverCommonConfig.tipArrowTimeCost;
			handler.updateCharge();
			handler.dirty = true;
			return arrow;
		}
		return null;
	}

	public static PotionCoreSkill findTipIndex(SkillSlotsHandler handler) {
		for (int i = 0; i < handler.getContainerSize(); i++) {
			if (handler.toggles.get(i) && handler.skills.get(i) instanceof PotionCoreSkill skill && skill.canBeToggled() && skill.progress >= EverCommonConfig.tipArrowTimeCost) {
				return skill;
			}
		}
		return null;
	}

	@Override
	public void startUsing(Player player, int slot) {
		if (player.level.isClientSide) {
			if (type == PotionType.LINGERING) {
				SkillSlotsClient.playSound(SoundEvents.UI_BUTTON_CLICK);
			} else if (EverCommonConfig.drinkDelay < 40) {
				SkillSlotsClient.playSound(SkillSlotsModule.USE_SHORT_SOUND.get());
			} else {
				SkillSlotsClient.playSound(SkillSlotsModule.USE_LONG_SOUND.get());
			}
		}
	}

	@Override
	public void finishUsing(Player player, int slot) {
		if (player.level.isClientSide) {
			return;
		}
		if (type == PotionType.NORMAL) {
			doEffect(effect, player, player);
			player.level.playSound(null, player, CoreModule.USE_NORMAL_SOUND.get(), SoundSource.PLAYERS, 1, 1);
		} else if (type == PotionType.SPLASH) {
			if (effect == null) {
				BlockPos pos = player.blockPosition();
				dowseFire(player, pos);
				dowseFire(player, pos.above());

				for (Direction direction : Direction.Plane.HORIZONTAL) {
					dowseFire(player, pos.relative(direction));
				}
			}
			AABB aabb = new AABB(player.position(), player.position()).inflate(4.0D, 2.0D, 4.0D);
			List<LivingEntity> list = player.level.getEntitiesOfClass(LivingEntity.class, aabb);
			if (!list.isEmpty()) {
				for (LivingEntity livingentity : list) {
					double d0 = player.distanceToSqr(livingentity);
					if (d0 < 16.0D) {
						doEffect(effect, player, livingentity);
					}
				}
			}
			boolean instant = effect != null && effect.getEffect().isInstantenous();
			SSplashPacket.send(player, color, instant);
		}
	}

	@Override
	public Component getDisplayNameInternal() {
		if (effect == null) {
			return Component.translatable("effect.none");
		}
		MutableComponent component = Component.translatable(effect.getDescriptionId());
		if (effect.getAmplifier() > 0) {
			component.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
		}
		return component;
	}

	@Override
	public Component getActionDescription() {
		if (type == PotionType.NORMAL) {
			return getDisplayName();
		}
		return Component.translatable(type.getDescKey());
	}

	@Override
	public boolean canBeToggled() {
		return type == PotionType.LINGERING && effect != null;
	}

	@Override
	public void onToggled(Player player, SkillSlotsHandler handler, int slot) {
		if (!handler.toggles.get(slot)) {
			return;
		}
		for (int i = 0; i < SkillSlotsHandler.MAX_SLOTS; i++) {
			if (i != slot && handler.toggles.get(i) && handler.skills.get(i) instanceof PotionCoreSkill skill && skill.canBeToggled()) {
				handler.toggles.clear(i);
			}
		}
	}

	@Override
	public int getChargeDuration(Player player) {
		return EverCommonConfig.refillTime;
	}

	@Override
	public int getUseDuration() {
		return EverCommonConfig.drinkDelay;
	}

	@Override
	public boolean isConflicting(Skill that) {
		if (that instanceof PotionCoreSkill) {
			return CoreItem.getEffect(item) == CoreItem.getEffect(that.item);
		}
		return false;
	}

	private void doEffect(MobEffectInstance effect, Player player, LivingEntity entity) {
		if (effect == null) {
			entity.clearFire();
			if (entity instanceof Axolotl axolotl) {
				axolotl.rehydrate();
			} else if (ThrownPotion.WATER_SENSITIVE.test(entity)) {
				entity.hurt(DamageSource.indirectMagic(entity, player), 1.0F);
			}
		} else {
			if (effect.getEffect().isInstantenous()) {
				effect.getEffect().applyInstantenousEffect(entity, player, entity, effect.getAmplifier(), 1.0D);
			} else {
				entity.addEffect(copyEffectWithSettings(effect));
			}
		}
	}

	// Copied from ThrownPotion
	private void dowseFire(Player player, BlockPos pos) {
		Level level = player.level;
		BlockState blockstate = level.getBlockState(pos);
		if (blockstate.is(BlockTags.FIRE)) {
			level.removeBlock(pos, false);
		} else if (AbstractCandleBlock.isLit(blockstate)) {
			AbstractCandleBlock.extinguish(player, blockstate, level, pos);
		} else if (CampfireBlock.isLitCampfire(blockstate)) {
			level.levelEvent(player, 1009, pos, 0);
			CampfireBlock.dowse(player, level, pos, blockstate);
			level.setBlockAndUpdate(pos, blockstate.setValue(CampfireBlock.LIT, Boolean.FALSE));
		}
	}
}
