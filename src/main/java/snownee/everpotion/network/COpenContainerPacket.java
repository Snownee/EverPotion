package snownee.everpotion.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.everpotion.container.PlaceContainer;
import snownee.kiwi.network.ClientPacket;

public class COpenContainerPacket extends ClientPacket {

    public static class Handler extends PacketHandler<COpenContainerPacket> {

        @Override
        public COpenContainerPacket decode(PacketBuffer buf) {
            return new COpenContainerPacket();
        }

        @Override
        public void encode(COpenContainerPacket pkt, PacketBuffer buf) {}

        @Override
        public void handle(COpenContainerPacket pkt, Supplier<Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ctx.get().getSender().openContainer(PlaceContainer.ContainerProvider.INSTANCE);
            });
            ctx.get().setPacketHandled(true);
        }

    }

}
