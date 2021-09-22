package snownee.everpotion.cap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import snownee.everpotion.handler.EverHandler;

public class EverCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

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
	public CompoundTag serializeNBT() {
		return handler.map($ -> $.serializeNBT()).orElseGet(CompoundTag::new);
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		handler.ifPresent($ -> $.deserializeNBT(nbt));
	}

}
