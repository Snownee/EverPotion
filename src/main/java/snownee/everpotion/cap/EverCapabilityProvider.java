package snownee.everpotion.cap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import snownee.everpotion.handler.EverHandler;

public class EverCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

	private final LazyOptional<EverHandler> handler;

	public EverCapabilityProvider(EverHandler handler) {
		this.handler = LazyOptional.of(() -> handler);
		this.handler.addListener($ -> {
			$.ifPresent(EverHandler::invalidate);
		});
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return EverCapabilities.HANDLER.orEmpty(cap, handler);
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
