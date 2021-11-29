package snownee.everpotion.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.everpotion.cap.EverCapabilities;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket("drink")
public class CDrinkPacket extends PacketHandler {
	public static CDrinkPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		int index = buf.readByte();
		return executor.apply(() -> {
			sender.getCapability(EverCapabilities.HANDLER).ifPresent(hander -> {
				if (hander.canUseSlot(index, true)) {
					hander.startDrinking(index);
				}
			});
		});
	}

	public static void send(int index) {
		I.sendToServer($ -> $.writeByte(index));
	}

}
