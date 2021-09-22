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
import net.minecraftforge.common.util.LazyOptional;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;

@Mixin(ArrowItem.class)
public abstract class MixinArrowItem /*extends Item*/ {

	//    public MixinArrowItem(Properties properties) {
	//        super(properties);
	//    }

	@Inject(at = @At("HEAD"), method = "createArrow", cancellable = true)
	public void everpotion_createArrow(Level worldIn, ItemStack stack, LivingEntity shooter, CallbackInfoReturnable<AbstractArrow> info) {
		if (stack.getItem() != Items.ARROW) {
			return;
		}
		LazyOptional<EverHandler> optional = shooter.getCapability(EverCapabilities.HANDLER);

		optional.ifPresent($ -> {
			AbstractArrow arrow = $.tryTipArrow(worldIn, stack);
			if (arrow != null) {
				info.setReturnValue(arrow);
			}
		});
	}

	//    @Override
	//    public boolean hasEffect(ItemStack stack) {
	//        if (stack.getItem() != Items.ARROW)
	//            return false;
	//        if (!FMLEnvironment.dist.isClient())
	//            return false;
	//        ClientPlayerEntity player = Minecraft.getInstance().player;
	//        if (player == null)
	//            return false;
	//        LazyOptional<EverHandler> optional = player.getCapability(EverCapabilities.HANDLER);
	//        if (!optional.isPresent())
	//            return false;
	//        EverHandler handler = optional.orElse(null);
	//        return handler.canUseSlot(handler.tipIndex, false);
	//    }

}
