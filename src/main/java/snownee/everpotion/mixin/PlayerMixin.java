package snownee.everpotion.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import snownee.skillslots.duck.SkillSlotsPlayer;
import snownee.skillslots.SkillSlotsHandler;

@Mixin(Player.class)
public class PlayerMixin implements SkillSlotsPlayer {

	private final SkillSlotsHandler everpotion$handler = new SkillSlotsHandler((Player) (Object) this);

	@Override
	public @Nullable SkillSlotsHandler skillslots$getHandler() {
		return everpotion$handler;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void everpotion$tick(CallbackInfo info) {
		everpotion$handler.tick();
	}

	@Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
	private void everpotion$readAdditionalSaveData(CompoundTag compound, CallbackInfo info) {
		if (compound.contains("EverPotion")) {
			everpotion$handler.deserializeNBT(compound.getCompound("EverPotion"));
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
	private void everpotion$addAdditionalSaveData(CompoundTag compound, CallbackInfo info) {
		compound.put("EverPotion", everpotion$handler.serializeNBT());
	}
}
