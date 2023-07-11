package snownee.everpotion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import snownee.everpotion.duck.EverPotionPlayer;
import snownee.everpotion.handler.EverHandler;

@Mixin(Entity.class)
public class EntityMixin {

	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void everpotion$setRemoved(Entity.RemovalReason removalReason, CallbackInfo ci) {
		if (this instanceof EverPotionPlayer player) {
			player.everpotion$getHandler().setOwner(null);
		}
	}

	@Inject(method = "unsetRemoved", at = @At("HEAD"))
	private void everpotion$unsetRemoved(CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if (entity instanceof Player player) {
			EverHandler.of(player).setOwner(player);
		}
	}
}
