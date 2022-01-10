package snownee.everpotion.network;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.everpotion.CoreModule;
import snownee.kiwi.network.Packet;

public class SSplashPacket extends Packet {

	private final BlockPos pos;
	private final int color;
	private final boolean instant;

	public SSplashPacket(BlockPos pos, int color, boolean instant) {
		this.pos = pos;
		this.color = color;
		this.instant = instant;
	}

	public static class Handler extends PacketHandler<SSplashPacket> {

		@Override
		public SSplashPacket decode(PacketBuffer buf) {
			return new SSplashPacket(buf.readBlockPos(), buf.readInt(), buf.readBoolean());
		}

		@Override
		public void encode(SSplashPacket pkt, PacketBuffer buf) {
			buf.writeBlockPos(pkt.pos);
			buf.writeInt(pkt.color);
			buf.writeBoolean(pkt.instant);
		}

		@Override
		public void handle(SSplashPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.world == null) {
					return;
				}
				mc.world.playSound(pkt.pos, CoreModule.USE_SPLASH_SOUND, SoundCategory.PLAYERS, 1, 1, false);
				if (mc.gameSettings.particles == ParticleStatus.MINIMAL) {
					return;
				}
				Vector3d vec3 = Vector3d.copyCenteredHorizontally(pkt.pos);
				Random random = mc.world.rand;
				float f3 = (float) (pkt.color >> 16 & 255) / 255.0F;
				float f4 = (float) (pkt.color >> 8 & 255) / 255.0F;
				float f5 = (float) (pkt.color >> 0 & 255) / 255.0F;
				IParticleData particleoptions = pkt.instant ? ParticleTypes.INSTANT_EFFECT : ParticleTypes.EFFECT;

				for (int j = 0; j < 100; ++j) {
					double d23 = random.nextDouble() * 4.0D;
					double d27 = random.nextDouble() * Math.PI * 2.0D;
					double d29 = Math.cos(d27) * d23;
					double d5 = 0.01D + random.nextDouble() * 0.5D;
					double d7 = Math.sin(d27) * d23;
					Particle particle = mc.particles.addParticle(particleoptions, vec3.x + d29 * 0.1D, vec3.y + 0.3D, vec3.z + d7 * 0.1D, d29, d5, d7);
					if (particle != null) {
						float f2 = 0.75F + random.nextFloat() * 0.25F;
						particle.setColor(f3 * f2, f4 * f2, f5 * f2);
						particle.multiplyVelocity((float) d23);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}

	}

}
