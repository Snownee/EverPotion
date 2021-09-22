package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import snownee.everpotion.menu.PlaceMenu;
import snownee.kiwi.network.ClientPacket;

public class COpenContainerPacket extends ClientPacket {

	public static class Handler extends PacketHandler<COpenContainerPacket> {

		@Override
		public COpenContainerPacket decode(FriendlyByteBuf buf) {
			return new COpenContainerPacket();
		}

		@Override
		public void encode(COpenContainerPacket pkt, FriendlyByteBuf buf) {
		}

		@Override
		public void handle(COpenContainerPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ctx.get().getSender().openMenu(PlaceMenu.ContainerProvider.INSTANCE);
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_SERVER;
		}

	}

}
