package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import snownee.everpotion.cap.EverCapabilities;
import snownee.kiwi.network.ClientPacket;

public class CDrinkPacket extends ClientPacket {

	private final int index;

	public CDrinkPacket(int index) {
		this.index = index;
	}

	public static class Handler extends PacketHandler<CDrinkPacket> {

		@Override
		public CDrinkPacket decode(FriendlyByteBuf buf) {
			return new CDrinkPacket(buf.readByte());
		}

		@Override
		public void encode(CDrinkPacket pkt, FriendlyByteBuf buf) {
			buf.writeByte(pkt.index);
		}

		@Override
		public void handle(CDrinkPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer sender = ctx.get().getSender();
				sender.getCapability(EverCapabilities.HANDLER).ifPresent(hander -> {
					if (hander.canUseSlot(pkt.index, true)) {
						hander.startDrinking(pkt.index);
					}
				});
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_SERVER;
		}

	}

}
