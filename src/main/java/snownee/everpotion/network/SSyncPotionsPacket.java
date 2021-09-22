package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.network.Packet;

public class SSyncPotionsPacket extends Packet {

	private final ServerPlayer player;
	private final EverHandler handler;

	public SSyncPotionsPacket(ServerPlayer player) {
		this.player = player;
		this.handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
	}

	public SSyncPotionsPacket(EverHandler handler) {
		this.player = null;
		this.handler = handler;
	}

	@Override
	public void send() {
		if (handler != null) {
			send(player);
		}
	}

	public static class Handler extends PacketHandler<SSyncPotionsPacket> {

		@Override
		public SSyncPotionsPacket decode(FriendlyByteBuf buf) {
			EverHandler handler = new EverHandler();
			int slots = buf.readByte();
			handler.setSlots(slots);
			for (int i = 0; i < slots; i++) {
				handler.setStackInSlot(i, buf.readItem());
				float progress = buf.readFloat();
				if (handler.caches[i] != null) {
					handler.caches[i].progress = progress;
				}
			}
			handler.chargeIndex = buf.readByte();
			handler.tipIndex = buf.readByte();
			handler.acceleration = buf.readFloat();
			return new SSyncPotionsPacket(handler);
		}

		@Override
		public void encode(SSyncPotionsPacket pkt, FriendlyByteBuf buf) {
			int slots = pkt.handler.getSlots();
			buf.writeByte(slots);
			for (int i = 0; i < slots; i++) {
				buf.writeItem(pkt.handler.getStackInSlot(i));
				buf.writeFloat(pkt.handler.caches[i] == null ? 0 : pkt.handler.caches[i].progress);
			}
			buf.writeByte(pkt.handler.chargeIndex);
			buf.writeByte(pkt.handler.tipIndex);
			buf.writeFloat(pkt.handler.acceleration);
		}

		@Override
		public void handle(SSyncPotionsPacket pkt, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Minecraft.getInstance().player.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
					handler.copyFrom(pkt.handler);
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
