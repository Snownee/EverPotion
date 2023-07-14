package snownee.skillslots.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.skillslots.SkillSlotsCommonConfig;
import snownee.skillslots.menu.PlaceMenu;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket("open_container")
public class COpenContainerPacket extends PacketHandler {
	public static COpenContainerPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		if (!SkillSlotsCommonConfig.playerCustomizable)
			return null;
		return executor.apply(() -> {
			sender.openMenu(PlaceMenu.ContainerProvider.INSTANCE);
		});
	}

}
