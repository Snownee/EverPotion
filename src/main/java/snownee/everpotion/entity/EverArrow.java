package snownee.everpotion.entity;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EverArrow extends Arrow {

	public EverArrow(EntityType<? extends EverArrow> type, Level worldIn) {
		super(type, worldIn);
	}

	public EverArrow(Level worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
	}

	public EverArrow(Level worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	public void setEffectsFromItem(ItemStack stack) {
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
