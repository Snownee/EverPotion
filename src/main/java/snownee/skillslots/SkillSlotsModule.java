package snownee.skillslots;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.skillslots.item.UnlockSlotItem;
import snownee.skillslots.menu.PlaceMenu;
import snownee.skillslots.network.SAbortUsingPacket;
import snownee.skillslots.network.SSyncSlotsPacket;
import snownee.skillslots.skill.Skill;
import snownee.skillslots.util.ClientProxy;
import snownee.skillslots.util.CommonProxy;

@KiwiModule(SkillSlots.ID)
public class SkillSlotsModule extends AbstractModule {
	public static final TagKey<Item> SKILL = itemTag(SkillSlots.ID, "skill");
	@KiwiModule.Category("misc")
	@KiwiModule.Name("skillslots:unlock_slot")
	public static final KiwiGO<UnlockSlotItem> UNLOCK_SLOT = go(UnlockSlotItem::new);
	public static final KiwiGO<MenuType<PlaceMenu>> PLACE = go(() -> new MenuType<>(PlaceMenu::new));
	public static final KiwiGO<SoundEvent> HOVER_SOUND = go(() -> new SoundEvent(new ResourceLocation(SkillSlots.ID, "hover")));
	public static final KiwiGO<SoundEvent> POTION_CHARGE_COMPLETE_SOUND = go(() -> new SoundEvent(new ResourceLocation(SkillSlots.ID, "potion_charge_complete")));
	public static final KiwiGO<SoundEvent> USE_SHORT_SOUND = go(() -> new SoundEvent(new ResourceLocation(SkillSlots.ID, "use_short")));
	public static final KiwiGO<SoundEvent> USE_LONG_SOUND = go(() -> new SoundEvent(new ResourceLocation(SkillSlots.ID, "use_long")));

	public static void sync(ServerPlayer player) {
		if (CommonProxy.isFakePlayer(player)) {
			return;
		}
		SSyncSlotsPacket.send(player);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(SkillSlotsCommand.init(dispatcher));
	}

	public static void playerLoggedIn(ServerPlayer player) {
		sync(player);
	}

	public static void causeDamage(DamageSource source, LivingEntity target, float amount) {
		if (target.level.isClientSide) {
			return;
		}
		if (!(source instanceof EntityDamageSource)) {
			return;
		}

		if (SkillSlotsCommonConfig.interruptedOnHurt && target instanceof ServerPlayer) {
			SkillSlotsHandler targetHandler = SkillSlotsHandler.of(target);
			if (targetHandler != null) {
				targetHandler.abortUsing();
				SAbortUsingPacket.I.send((ServerPlayer) target, $ -> {
				});
			}
		}

		if (SkillSlotsCommonConfig.damageAcceleration > 0 && source.getEntity() instanceof ServerPlayer sourceEntity) {
			SkillSlotsHandler.of(sourceEntity).accelerate(.05f * amount * (float) SkillSlotsCommonConfig.damageAcceleration);
		}
	}

	public static void clonePlayer(Player original, Player clone) {
		SkillSlotsHandler newHandler = SkillSlotsHandler.of(clone);
		SkillSlotsHandler oldHandler = SkillSlotsHandler.of(original);
		newHandler.copyFrom(oldHandler);
	}

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> SkillSlots.SKILL_FACTORIES.add(Skill::new));
	}

	@Override
	protected void clientInit(ClientInitEvent event) {
		event.enqueueWork(ClientProxy::loadComplete);
	}


}
