package snownee.everpotion.cap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import snownee.everpotion.inventory.EverHandler;

public class EverCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    private final LazyOptional<EverHandler> handler;

    public EverCapabilityProvider(EverHandler handler) {
        this.handler = LazyOptional.of(() -> handler);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == EverCapabilities.HANDLER ? handler.cast() : null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return handler.map($ -> $.serializeNBT()).orElseGet(CompoundNBT::new);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        handler.ifPresent($ -> $.deserializeNBT(nbt));
    }

}
