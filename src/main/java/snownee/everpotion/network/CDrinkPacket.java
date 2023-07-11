package snownee.everpotion.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket("drink")
public class CDrinkPacket extends PacketHandler {
	public static CDrinkPacket I;

	public static void send(int index) {
		I.sendToServer($ -> $.writeByte(index));
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		int index = buf.readByte();
		return executor.apply(() -> {
			EverHandler handler = EverHandler.of(sender);
			if (handler.canUseSlot(index, true)) {
				handler.startDrinking(index);
			}
		});
	}

}
