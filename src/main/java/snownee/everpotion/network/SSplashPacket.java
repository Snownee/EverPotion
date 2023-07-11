package snownee.everpotion.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import snownee.everpotion.CoreModule;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "splash", dir = Direction.PLAY_TO_CLIENT)
public class SSplashPacket extends PacketHandler {
	public static SSplashPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		BlockPos pos = buf.readBlockPos();
		int color = buf.readInt();
		boolean instant = buf.readBoolean();
		return executor.apply(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}

			mc.level.playLocalSound(pos, CoreModule.USE_SPLASH_SOUND.get(), SoundSource.PLAYERS, 1, 1, false);
			if (mc.options.particles().get() == ParticleStatus.MINIMAL) {
				return;
			}
			Vec3 vec3 = Vec3.atBottomCenterOf(pos);
			RandomSource random = mc.level.random;
			float f3 = (float) (color >> 16 & 255) / 255.0F;
			float f4 = (float) (color >> 8 & 255) / 255.0F;
			float f5 = (float) (color & 255) / 255.0F;
			ParticleOptions particleoptions = instant ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

			for (int j = 0; j < 100; ++j) {
				double d23 = random.nextDouble() * 4.0D;
				double d27 = random.nextDouble() * Math.PI * 2.0D;
				double d29 = Math.cos(d27) * d23;
				double d5 = 0.01D + random.nextDouble() * 0.5D;
				double d7 = Math.sin(d27) * d23;
				Particle particle = mc.particleEngine.createParticle(particleoptions, vec3.x + d29 * 0.1D, vec3.y + 0.3D, vec3.z + d7 * 0.1D, d29, d5, d7);
				if (particle != null) {
					float f2 = 0.75F + random.nextFloat() * 0.25F;
					particle.setColor(f3 * f2, f4 * f2, f5 * f2);
					particle.setPower((float) d23);
				}
			}

		});
	}

	public static void send(Entity entity, int color, boolean instant) {
		I.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), buf -> {
			buf.writeBlockPos(entity.blockPosition());
			buf.writeInt(color);
			buf.writeBoolean(instant);
		});
	}

}
