package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.client.gui.UseScreen;
import snownee.kiwi.network.Packet;

public class SCancelPacket extends Packet {

    public SCancelPacket() {}

    public static class Handler extends PacketHandler<SCancelPacket> {

        @Override
        public SCancelPacket decode(PacketBuffer buf) {
            return new SCancelPacket();
        }

        @Override
        public void encode(SCancelPacket pkt, PacketBuffer buf) {}

        @Override
        public void handle(SCancelPacket pkt, Supplier<Context> ctx) {
            ctx.get().enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) {
                    return;
                }
                mc.player.getCapability(EverCapabilities.HANDLER).ifPresent(handler -> {
                    handler.stopDrinking();
                    if (mc.currentScreen instanceof UseScreen) {
                        mc.displayGuiScreen(null);
                    }
                });
            });
            ctx.get().setPacketHandled(true);
        }

    }

}
