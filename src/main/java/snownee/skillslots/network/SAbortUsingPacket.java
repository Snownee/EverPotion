package snownee.skillslots.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;
import snownee.skillslots.SkillSlotsHandler;
import snownee.skillslots.client.gui.UseScreen;

@KiwiPacket(value = "abort_using", dir = Direction.PLAY_TO_CLIENT)
public class SAbortUsingPacket extends PacketHandler {
	public static SAbortUsingPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		return executor.apply(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			SkillSlotsHandler handler = SkillSlotsHandler.of(Minecraft.getInstance().player);
			handler.abortUsing();
			if (mc.screen instanceof UseScreen) {
				mc.setScreen(null);
			}
		});
	}

}
