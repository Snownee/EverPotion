package snownee.everpotion.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import snownee.everpotion.handler.EverHandler;

@Mixin(ArrowItem.class)
public abstract class ArrowItemMixin {

	@Inject(at = @At("HEAD"), method = "createArrow", cancellable = true)
	public void everpotion$createArrow(Level worldIn, ItemStack stack, LivingEntity shooter, CallbackInfoReturnable<AbstractArrow> info) {
		if (!stack.is(Items.ARROW)) {
			return;
		}
		EverHandler handler = EverHandler.of(shooter);
		if (handler != null) {
			AbstractArrow arrow = handler.tryTipArrow(worldIn, stack);
			if (arrow != null) {
				info.setReturnValue(arrow);
			}
		}
	}

}
