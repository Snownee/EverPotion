package snownee.skillslots.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.skillslots.SkillSlotsHandler;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket("start_using")
public class CStartUsingPacket extends PacketHandler {
	public static CStartUsingPacket I;

	public static void send(int index) {
		I.sendToServer($ -> $.writeByte(index));
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		int index = buf.readByte();
		return executor.apply(() -> {
			SkillSlotsHandler handler = SkillSlotsHandler.of(sender);
			if (handler.canUseSlot(index)) {
				handler.startUsing(index);
			}
		});
	}

}
