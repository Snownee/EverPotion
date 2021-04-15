package snownee.everpotion.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EverArrowEntity extends ArrowEntity {

	public EverArrowEntity(EntityType<? extends ArrowEntity> type, World worldIn) {
		super(type, worldIn);
	}

	public EverArrowEntity(World worldIn, LivingEntity shooter) {
		super(worldIn, shooter);
	}

	public EverArrowEntity(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	@Override
	public void setPotionEffect(ItemStack stack) {
	}

	@Override
	protected ItemStack getArrowStack() {
		return new ItemStack(Items.ARROW);
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
