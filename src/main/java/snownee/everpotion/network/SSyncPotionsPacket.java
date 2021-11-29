package snownee.everpotion.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "sync_potions", dir = Direction.PLAY_TO_CLIENT)
public class SSyncPotionsPacket extends PacketHandler {
	public static SSyncPotionsPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		EverHandler newHandler = new EverHandler();
		int slots = buf.readByte();
		newHandler.setSlots(slots);
		for (int i = 0; i < slots; i++) {
			newHandler.setStackInSlot(i, buf.readItem());
			float progress = buf.readFloat();
			if (newHandler.caches[i] != null) {
				newHandler.caches[i].progress = progress;
			}
		}
		newHandler.chargeIndex = buf.readByte();
		newHandler.tipIndex = buf.readByte();
		newHandler.acceleration = buf.readFloat();
		return executor.apply(() -> {
			Minecraft.getInstance().player.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
				handler.copyFrom(newHandler);
			});
		});
	}

	public static void send(ServerPlayer player) {
		EverHandler handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
		if (handler == null) {
			return;
		}
		I.send(player, buf -> {
			int slots = handler.getSlots();
			buf.writeByte(slots);
			for (int i = 0; i < slots; i++) {
				buf.writeItem(handler.getStackInSlot(i));
				buf.writeFloat(handler.caches[i] == null ? 0 : handler.caches[i].progress);
			}
			buf.writeByte(handler.chargeIndex);
			buf.writeByte(handler.tipIndex);
			buf.writeFloat(handler.acceleration);
		});
	}

}
