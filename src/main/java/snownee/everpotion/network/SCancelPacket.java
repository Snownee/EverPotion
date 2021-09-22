package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.gui.UseScreen;
import snownee.kiwi.network.Packet;

public class SCancelPacket extends Packet {

	public SCancelPacket() {
	}

	public static class Handler extends PacketHandler<SCancelPacket> {

		@Override
		public SCancelPacket decode(FriendlyByteBuf buf) {
			return new SCancelPacket();
		}

		@Override
		public void encode(SCancelPacket pkt, FriendlyByteBuf buf) {
		}

		@Override
		public void handle(SCancelPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player == null) {
					return;
				}
				mc.player.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
					handler.stopDrinking();
					if (mc.screen instanceof UseScreen) {
						mc.setScreen(null);
					}
				});
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_CLIENT;
		}

	}

}
