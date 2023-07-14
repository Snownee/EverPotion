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
import snownee.skillslots.skill.Skill;

@KiwiPacket(value = "sync_slots", dir = Direction.PLAY_TO_CLIENT)
public class SSyncSlotsPacket extends PacketHandler {
	public static SSyncSlotsPacket I;

	public static void send(ServerPlayer player) {
		SkillSlotsHandler handler = SkillSlotsHandler.of(player);
		handler.dirty = false;
		I.send(player, buf -> {
			int slots = handler.getContainerSize();
			buf.writeByte(slots);
			for (int i = 0; i < slots; i++) {
				buf.writeItem(handler.getItem(i));
				Skill skill = handler.skills.get(i);
				buf.writeFloat(skill.progress);
			}
			buf.writeByte(handler.chargeIndex);
			buf.writeBitSet(handler.toggles);
			buf.writeFloat(handler.acceleration);
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer sender) {
		SkillSlotsHandler newHandler = new SkillSlotsHandler();
		int slots = buf.readByte();
		newHandler.setSlots(slots);
		for (int i = 0; i < slots; i++) {
			newHandler.setItem(i, buf.readItem());
			Skill skill = newHandler.skills.get(i);
			skill.progress = buf.readFloat();
		}
		newHandler.chargeIndex = buf.readByte();
		newHandler.toggles = buf.readBitSet();
		newHandler.acceleration = buf.readFloat();
		return executor.apply(() -> {
			SkillSlotsHandler handler = SkillSlotsHandler.of(Minecraft.getInstance().player);
			handler.copyFrom(newHandler);
		});
	}

}
