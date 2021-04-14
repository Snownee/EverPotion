package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.everpotion.cap.EverCapabilities;
import snownee.kiwi.network.ClientPacket;

public class CDrinkPacket extends ClientPacket {

    private final int index;

    public CDrinkPacket(int index) {
        this.index = index;
    }

    public static class Handler extends PacketHandler<CDrinkPacket> {

        @Override
        public CDrinkPacket decode(PacketBuffer buf) {
            return new CDrinkPacket(buf.readByte());
        }

        @Override
        public void encode(CDrinkPacket pkt, PacketBuffer buf) {
            buf.writeByte(pkt.index);
        }

        @Override
        public void handle(CDrinkPacket pkt, Supplier<Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                sender.getCapability(EverCapabilities.HANDLER).ifPresent(hander -> {
                    if (hander.canUseSlot(pkt.index, true)) {
                        hander.startDrinking(pkt.index);
                    }
                });
            });
            ctx.get().setPacketHandled(true);
        }

    }

}
