package snownee.everpotion.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.everpotion.client.gui.UseScreen;
import snownee.everpotion.handler.EverHandler;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.KiwiPacket.Direction;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "cancel", dir = Direction.PLAY_TO_CLIENT)
public class SCancelPacket extends PacketHandler {
	public static SCancelPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		return executor.apply(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			EverHandler handler = EverHandler.of(Minecraft.getInstance().player);
			handler.stopDrinking();
			if (mc.screen instanceof UseScreen) {
				mc.setScreen(null);
			}
		});
	}

}
